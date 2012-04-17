/*
 * Copyright (C) 2010-2012 Eco Mobile Citizen
 *
 * This file is part of EcoCitizen.
 *
 * EcoCitizen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EcoCitizen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EcoCitizen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ecocitizen.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.BluetoothDevice;

import com.ecocitizen.common.MessageType;
import com.ecocitizen.common.bundlewrapper.NoteBundleWrapper;
import com.ecocitizen.common.bundlewrapper.SensorInfoBundleWrapper;

public class DeviceManagerService extends Service {
	// Debugging
	private static final String TAG = "DeviceManagerService";
	private static final boolean D = false;
	
	/**
	 * List of callbacks that will receive notification on events:
	 * - sensor connected, disconnected, lost, ...
	 * - data received from sensor
	 */
	private final RemoteCallbackList<IDeviceManagerServiceCallback> mCallbacks =
		new RemoteCallbackList<IDeviceManagerServiceCallback>();

	/**
	 * Mapping of device id -> SensorManager
	 * Many sensors can be connected at the same time, this hashmap
	 * keeps track of connected (alive) sensors.
	 * 
	 * Important: sensors can be added and removed via RPC calls by clients,
	 * and defunct sensors are removed automatically. All these operations might
	 * not always happen on the same thread, and therefore need synchronization. 
	 */
	private HashMap<String, SensorManager> mSensorManagers = new HashMap<String, SensorManager>();

	/**
	 * Location listener, to get GPS location updates.
	 * This object is passed to the constructor of SensorManager objects,
	 * so that they attach GPS coordinates to their messages.
	 * 
	 * The location listener needs to be member object, because
	 * Android location services are accessible through Context.
	 */
	private GpsLocationListener mGpsLocationListener;

	@Override
	public IBinder onBind(Intent intent) {
		if (D) Log.d(TAG, "+++ ON BIND +++");
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		if (D) Log.d(TAG, "+++ ON REBIND +++");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (D) Log.d(TAG, "+++ ON UNBIND +++");
		return super.onUnbind(intent);
	}

	/**
	 * When DeviceManager is created, create and initialize a location manager.
	 * The location manager is used to attach GPS coordinates to incoming sensor data.
	 */
	@Override
	public void onCreate() {
		mGpsLocationListener = new GpsLocationListener((LocationManager)getSystemService(Context.LOCATION_SERVICE));
	}

	/**
	 * Shutdown all related objects:
	 * - all connected sensors
	 * - all callback listeners
	 * - remove location updates
	 */
	@Override
	public void onDestroy() {
		if (D) Log.d(TAG, "+++ ON DESTROY +++");

		shutdownAllSensorManagers();
		mGpsLocationListener.removeLocationUpdates();

		Toast.makeText(this, "Device Manager stopped", Toast.LENGTH_SHORT);

		mCallbacks.kill();
	}

	/**
	 * Handling of our service methods.
	 */
	private final IDeviceManagerService.Stub mBinder = new IDeviceManagerService.Stub() {
		/**
		 * Connect to a bluetooth device. 
		 * The device connection is represented by a BluetoothDevice object.
		 */
		public void connectBluetoothDevice(BluetoothDevice device) {
			String deviceId = BluetoothSensorManager.getDeviceId(device);
			synchronized (mSensorManagers) {
				if (mSensorManagers.containsKey(deviceId)) return;

				try {
					BluetoothSensorManager sm = 
						new BluetoothSensorManager(mHandler, mGpsLocationListener, device);
					sm.connect();
					mSensorManagers.put(deviceId, sm);
				}
				catch (Exception e) {
					// Success or failure will be communicated back 
					// to the caller by messages on the Handler.
					return;
				}
			}
		}

		/**
		 * Connect to a logfile player, useful for debugging.
		 * The logfile name is used as the device id.
		 * The message interval parameter controls the data transmission speed,
		 * useful for stress tests.
		 */
		public void connectLogplayer(String assetFilename, int messageInterval) {
			String deviceId = assetFilename;
			synchronized (mSensorManagers) {
				if (mSensorManagers.containsKey(deviceId)) return;

				try {
					InputStream instream = getAssets().open(assetFilename);
					LogplayerSensorManager sm = new LogplayerSensorManager(mHandler, mGpsLocationListener, instream, messageInterval, assetFilename);
					if (sm.connect()) {
						mSensorManagers.put(deviceId, sm);
					}
				}
				catch (IOException e) {
					// Success or failure will be communicated back 
					// to the caller by messages on the Handler.
					e.printStackTrace();
					return;
				}
			}
		}

		/**
		 * Dummy device, useful for simple GPS tracking, and when
		 * there is no real sensor to connect to the application.
		 * The message interval parameter is the waiting period between dummy messages.
		 */
		public void connectDummyDevice(int messageInterval) {
			String deviceId = DummySensorManager.SENSOR_ID;
			synchronized (mSensorManagers) {
				if (mSensorManagers.containsKey(deviceId)) return;

				DummySensorManager sm = new DummySensorManager(mHandler, mGpsLocationListener, messageInterval);
				if (sm.connect()) {
					mSensorManagers.put(deviceId, sm);
				}
			}
		}

		public void shutdown() throws RemoteException {
			shutdownAllSensorManagers();
			stopSelf();
		}

		/**
		 * Disconnect a device specified by device id:
		 * - Bluetooth MAC address for bluetooth devices
		 * - filename for logfile players 
		 */
		public void disconnectDevice(String deviceId) throws RemoteException {
			if (deviceId == null) {
				synchronized (mSensorManagers) {
					if (!mSensorManagers.isEmpty()) {
						deviceId = mSensorManagers.keySet().iterator().next();
						shutdownSensorManager(deviceId);
					}
				}
			}
			else {
				shutdownSensorManager(deviceId);
			}
		}

		public void registerCallback(IDeviceManagerServiceCallback cb) {
			if (cb != null) mCallbacks.register(cb);
		}

		public void unregisterCallback(IDeviceManagerServiceCallback cb) {
			if (cb != null) mCallbacks.unregister(cb);
		}

		public int getPid() {
			return Process.myPid();
		}

		public void addNote(Bundle startLocationBundle, String note)
				throws RemoteException {
			Bundle bundle =
				NoteBundleWrapper.makeBundle(startLocationBundle, mGpsLocationListener.getLastLocationBundle(), note);
			mHandler.obtainMessage(MessageType.NOTE, bundle).sendToTarget();
		}

		public Bundle getLocationBundle() throws RemoteException {
			return mGpsLocationListener.getLastLocationBundle();
		}

		public Bundle[] getConnectedDevices() throws RemoteException {
			List<Bundle> bundles = new LinkedList<Bundle>();
			synchronized (mSensorManagers) {
				for (SensorManager sensorManager : mSensorManagers.values()) {
					bundles.add(SensorInfoBundleWrapper.makeBundle(sensorManager.getDeviceName(), sensorManager.getDeviceId()));
				}
			}
			return bundles.toArray(new Bundle[]{});
		}
	};

	/**
	 * Our Handler to execute operations on the main thread.
	 * 
	 * The handler is shared to all SensorManager objects to 
	 * report their connection states and to dispatch to callback listeners.
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SM_DEVICE_ADDED:
			{
				final SensorInfoBundleWrapper sensorInfo = new SensorInfoBundleWrapper((Bundle)msg.obj);
				if (D) Log.d(TAG, "what = " + msg.what + ", deviceName = " + sensorInfo.getSensorName());
				mGpsLocationListener.requestLocationUpdates();
				final int N = mCallbacks.beginBroadcast();
				for (int i = 0; i < N; ++i) {
					try {
						mCallbacks.getBroadcastItem(i).receivedDeviceAdded((Bundle)msg.obj);
					}
					catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing
						// the dead object for us.
					}
				}
				mCallbacks.finishBroadcast();
			} break;
			case MessageType.SM_CONNECTION_FAILED:
			case MessageType.SM_DEVICE_CLOSED: 
			case MessageType.SM_DEVICE_LOST: 
			{
				final String deviceId = (String)msg.obj;
				if (D) Log.d(TAG, "what = " + msg.what + ", deviceName = " + getDeviceName(deviceId));
				shutdownSensorManager(deviceId);
				final int N = mCallbacks.beginBroadcast();
				for (int i = 0; i < N; ++i) {
					try {
						switch (msg.what) {
						case MessageType.SM_CONNECTION_FAILED:
							mCallbacks.getBroadcastItem(i).receivedConnectionFailed(deviceId);
							break;
						case MessageType.SM_DEVICE_CLOSED:
							mCallbacks.getBroadcastItem(i).receivedDeviceClosed(deviceId);
							if (mSensorManagers.isEmpty()) {
								mCallbacks.getBroadcastItem(i).receivedAllDevicesGone();
							}
							break;
						case MessageType.SM_DEVICE_LOST:
							mCallbacks.getBroadcastItem(i).receivedDeviceLost(deviceId);
							if (mSensorManagers.isEmpty()) {
								mCallbacks.getBroadcastItem(i).receivedAllDevicesGone();
							}
							break;
						}
					}
					catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing
						// the dead object for us.
					}
				}
				mCallbacks.finishBroadcast();
			} break;
			case MessageType.SENSOR_DATA: {
				// Broadcast to all clients
				final int N = mCallbacks.beginBroadcast();
				final Bundle bundle = (Bundle)msg.obj;
				for (int i = 0; i < N; ++i) {
					try {
						mCallbacks.getBroadcastItem(i).receivedSensorDataBundle(bundle);
					}
					catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing
						// the dead object for us.
					}
				}
				mCallbacks.finishBroadcast();
			} break;
			case MessageType.NOTE: {
				// Broadcast to all clients
				final int N = mCallbacks.beginBroadcast();
				final Bundle bundle = (Bundle)msg.obj;
				for (int i = 0; i < N; ++i) {
					try {
						mCallbacks.getBroadcastItem(i).receivedNoteBundle(bundle);
					}
					catch (RemoteException e) {
						// The RemoteCallbackList will take care of removing
						// the dead object for us.
					}
				}
				mCallbacks.finishBroadcast();
			} break;
			default:
				super.handleMessage(msg);
			}
		}
	};

	private void shutdownAllSensorManagers() {
		synchronized (mSensorManagers) {
			for (SensorManager sm : mSensorManagers.values()) {
				sm.shutdown();
			}
			mSensorManagers.clear();
		}
	}

	private void shutdownSensorManager(String deviceId) {
		synchronized (mSensorManagers) {
			if (mSensorManagers.containsKey(deviceId)) {
				mSensorManagers.get(deviceId).shutdown();
				mSensorManagers.remove(deviceId);
				
				if (mSensorManagers.isEmpty()) {
					mGpsLocationListener.removeLocationUpdates();
				}
			}
		}
	}
	
	private String getDeviceName(String deviceId) {
		try {
			return mSensorManagers.get(deviceId).getDeviceName();
		}
		catch (Exception e) {
			return null;
		}
	}
	
}
