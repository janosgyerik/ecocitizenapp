/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
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
import backport.android.bluetooth.BluetoothDevice;

public class DeviceManagerService extends Service {
	// Debugging
	private static final String TAG = "DeviceManagerService";
	private static final boolean D = true;
	private static final boolean LOG_SENTENCES = false;

	final RemoteCallbackList<IDeviceManagerServiceCallback> mCallbacks =
		new RemoteCallbackList<IDeviceManagerServiceCallback>();

	HashMap<String, SensorManager> mSensorManagers = new HashMap<String, SensorManager>();

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

	@Override
	public void onCreate() {
		initLocationManager();
	}

	@Override
	public void onDestroy() {
		if (D) Log.d(TAG, "+++ ON DESTROY +++");

		shutdownAllSensorManagers();

		Toast.makeText(this, "Device Manager stopped", Toast.LENGTH_SHORT);

		mCallbacks.kill();
	}

	private final IDeviceManagerService.Stub mBinder = new IDeviceManagerService.Stub() {
		public void connectBluetoothDevice(BluetoothDevice device) {
			String name = device.getName();
			synchronized (mSensorManagers) {
				if (mSensorManagers.containsKey(name)) return;

				SensorManager sm = new BluetoothSensorManager(mHandler, mLocationListener, device);
				mSensorManagers.put(name, sm);
			}
		}

		public void connectLogplayer(String assetFilename, int messageInterval) {
			String name = assetFilename;
			synchronized (mSensorManagers) {
				if (mSensorManagers.containsKey(name)) return;

				try {
					InputStream instream = getAssets().open(assetFilename);
					SensorManager sm = new LogplayerSensorManager(mHandler, mLocationListener, instream, messageInterval, assetFilename);
					sm.start();
					mSensorManagers.put(name, sm);
				}
				catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}

		public void shutdown() throws RemoteException {
			stopSelf();
		}

		public void disconnectDevice(String deviceName) throws RemoteException {
			shutdownSensorManager(deviceName);
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

		public String getConnectedDeviceName() throws RemoteException {
			// TODO add support for multiple devices
			for (SensorManager sm : mSensorManagers.values()) {
				return sm.mDeviceName;
			}
			return null;
		}
	};

	GpsLocationListener mLocationListener = new GpsLocationListener();

	/**
	 * Our Handler to execute operations on the main thread.
	 * This is used to dispatch sentences to the callbacks.
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENSORCONNECTION_NONE:
			case MessageType.SENSORCONNECTION_SUCCESS:
			case MessageType.SENSORCONNECTION_FAILED:
			case MessageType.SENSORCONNECTION_LOST: 
			case MessageType.SENSORCONNECTION_DISCONNECTSELF: {
				final String deviceName = (String)msg.obj;
				if (msg.what == MessageType.SENSORCONNECTION_SUCCESS) {
					mLocationListener.requestLocationUpdates();
				}
				else {
					shutdownSensorManager(deviceName);
				}
				final int N = mCallbacks.beginBroadcast();
				if (D) Log.d(TAG, "what = " + msg.what + ", deviceName = " + deviceName);
				for (int i = 0; i < N; ++i) {
					try {
						switch (msg.what) {
						case MessageType.SENSORCONNECTION_NONE:
							mCallbacks.getBroadcastItem(i).receivedSensorConnectionNone();
							break;
						case MessageType.SENSORCONNECTION_SUCCESS:
							mCallbacks.getBroadcastItem(i).receivedSensorConnectionSuccess(deviceName);
							break;
						case MessageType.SENSORCONNECTION_FAILED:
							mCallbacks.getBroadcastItem(i).receivedSensorConnectionFailed(deviceName);
							break;
						case MessageType.SENSORCONNECTION_LOST:
							mCallbacks.getBroadcastItem(i).receivedSensorConnectionLost(deviceName);
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
			case MessageType.SENTENCE: {
				// Broadcast to all clients
				final int N = mCallbacks.beginBroadcast();
				final Bundle bundle = (Bundle)msg.obj;
				final String sentence = (String)bundle.getString(BundleKeys.SENTENCE_LINE);
				if (LOG_SENTENCES) Log.d(TAG, "SENTENCE = " + sentence);
				for (int i = 0; i < N; ++i) {
					try {
						mCallbacks.getBroadcastItem(i).receivedSentenceBundle(bundle);
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

	void shutdownAllSensorManagers() {
		mLocationListener.removeLocationUpdates();
		
		synchronized (mSensorManagers) {
			for (SensorManager sm : mSensorManagers.values()) {
				sm.stop();
			}
			mSensorManagers.clear();
		}
	}

	void shutdownSensorManager(String deviceName) {
		if (mSensorManagers.containsKey(deviceName)) {
			synchronized (mSensorManagers) {
				mSensorManagers.get(deviceName).stop();
				mSensorManagers.remove(deviceName);
				
				if (mSensorManagers.isEmpty()) {
					mLocationListener.removeLocationUpdates();
				}
			}
		}
	}

	public void initLocationManager() {
		mLocationListener.setLocationManager((LocationManager)getSystemService(Context.LOCATION_SERVICE));
	}

}
