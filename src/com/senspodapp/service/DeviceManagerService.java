package com.senspodapp.service;

import java.io.IOException;

import java.io.InputStream;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothDevice;

public class DeviceManagerService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	final RemoteCallbackList<IDeviceManagerServiceCallback> mCallbacks =
		new RemoteCallbackList<IDeviceManagerServiceCallback>();
	
	BluetoothSensorService mBluetoothSensorService = null;
	LogplayerService mLogplayerService = null;
	
	private final IDeviceManagerService.Stub mBinder = new IDeviceManagerService.Stub() {
		public void connectBluetoothDevice(BluetoothDevice device) {
			// TODO: add support for multiple devices
			if (mBluetoothSensorService == null) {
				mBluetoothSensorService = new BluetoothSensorService(mHandler);
				mBluetoothSensorService.connect(device);
			}
		}

		public void disconnectBluetoothDevice(String deviceName) {
			// TODO: add support for multiple devices
			shutdownBluetoothSensorService();
		}

		public void registerCallback(IDeviceManagerServiceCallback cb) {
			if (cb != null) mCallbacks.register(cb);
		}

		public void unregisterCallback(IDeviceManagerServiceCallback cb) {
			if (cb != null) mCallbacks.unregister(cb);
		}

		public void connectLogplayer(String assetFilename, int messageInterval) {
			// TODO: add support for multiple devices
			if (mLogplayerService == null) {
				try {
					InputStream instream = getAssets().open(assetFilename);
					mLogplayerService = new LogplayerService(mHandler, instream, messageInterval);
					mLogplayerService.connect(null);
				}
				catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}

		public void disconnectLogplayer() {
		}

		public int getPid() {
			return Process.myPid();
		}
	};

	private static final int SENTENCE_MSG = 1;
	/**
	 * Our Handler to execute operations on the main thread.
	 * This is used to dispatch sentences to the callbacks.
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SENTENCE_MSG: {
					// Broadcast to all clients
					final int N = mCallbacks.beginBroadcast();
					final String sentence = new String((byte[])msg.obj);
					for (int i = 0; i < N; ++i) {
						try {
							mCallbacks.getBroadcastItem(i).receivedSentenceData("sensorid", sentence);
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
	
	@Override
	public void onCreate() {
	}
	
	@Override
	public void onDestroy() {
		shutdownBluetoothSensorService();
		
		Toast.makeText(this, "Device Manager stopped", Toast.LENGTH_SHORT);
		
		mCallbacks.kill();
	}
	
	void shutdownBluetoothSensorService() {
		if (mBluetoothSensorService == null) return;
		mBluetoothSensorService.stop();
	}

}
