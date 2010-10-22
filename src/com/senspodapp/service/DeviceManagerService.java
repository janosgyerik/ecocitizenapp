package com.senspodapp.service;

import java.io.IOException;
import java.io.InputStream;

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

		shutdownBluetoothSensorService();
		shutdownLogplayer();

		Toast.makeText(this, "Device Manager stopped", Toast.LENGTH_SHORT);

		mCallbacks.kill();
	}

	final RemoteCallbackList<IDeviceManagerServiceCallback> mCallbacks =
		new RemoteCallbackList<IDeviceManagerServiceCallback>();

	BluetoothSensorService mBluetoothSensorService = null;
	SensorManager mLogplayerService = null;
	String mConnectedDeviceName = null;

	private final IDeviceManagerService.Stub mBinder = new IDeviceManagerService.Stub() {
		public void connectBluetoothDevice(BluetoothDevice device) {
			// TODO: add support for multiple devices
			if (mConnectedDeviceName != null) return;
			if (mBluetoothSensorService != null) return;

			mBluetoothSensorService = new BluetoothSensorService(mHandler);
			mBluetoothSensorService.connect(device);
		}

		public void disconnectBluetoothDevice(String deviceName) {
			// TODO: add support for multiple devices
			shutdownBluetoothSensorService();
		}

		public void connectLogplayer(String assetFilename, int messageInterval) {
			// TODO: add support for multiple devices
			if (mConnectedDeviceName != null) return;
			if (mLogplayerService != null) return;

			try {
				InputStream instream = getAssets().open(assetFilename);
				mLogplayerService = new LogplayerService(mHandler, mLocationListener, instream, messageInterval);
				mLogplayerService.start();
			}
			catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		public void disconnectLogplayer() {
			// TODO: add support for multiple devices
			shutdownLogplayer();
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
			return mConnectedDeviceName;
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
			case MessageType.SENSORCONNECTION_LOST: {
				final String deviceName = (String)msg.obj;
				mConnectedDeviceName = deviceName;
				if (msg.what == MessageType.SENSORCONNECTION_SUCCESS) {
					mLocationListener.requestLocationUpdates();
				}
				else {
					mLocationListener.removeLocationUpdates();
					mConnectedDeviceName = null;
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
				// TODO
				// SENTENCE message should include:
				// GpsInfo, DateTime, SensorId, Sentence 
				// Broadcast to all clients
				final int N = mCallbacks.beginBroadcast();
				final Bundle bundle = (Bundle)msg.obj;
				final String sentence = (String)bundle.getString(BundleKeys.SENTENCE_LINE);
				if (D) Log.d(TAG, "SENTENCE = " + sentence);
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

	void shutdownBluetoothSensorService() {
		if (mBluetoothSensorService == null) return;
		mBluetoothSensorService.stop();
		mBluetoothSensorService = null;
	}

	void shutdownLogplayer() {
		if (mLogplayerService == null) return;
		mLogplayerService.stop();
		mLogplayerService = null;
	}

	public void initLocationManager() {
		mLocationListener.setLocationManager((LocationManager)getSystemService(Context.LOCATION_SERVICE));
	}

}
