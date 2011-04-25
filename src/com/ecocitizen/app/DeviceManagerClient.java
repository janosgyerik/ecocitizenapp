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

package com.ecocitizen.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

import com.ecocitizen.service.BundleKeys;
import com.ecocitizen.service.IDeviceManagerService;
import com.ecocitizen.service.IDeviceManagerServiceCallback;
import com.ecocitizen.service.IFileSaverService;
import com.ecocitizen.service.ISensorMapUploaderService;
import com.ecocitizen.service.MessageType;

public abstract class DeviceManagerClient extends Activity {
	// Debugging
	private static final String TAG = "DeviceManagerClient";
	private static final boolean D = true;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private final static String PREFS_RTUPLOAD = "rtupload";
	private final static String PREFS_FILESAVER = "filesaver";

	// Layout Views
	TextView mTitle;

	// Local Bluetooth adapter
	BluetoothAdapter mBluetoothAdapter = null;
	
	String mConnectedDeviceName = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "++ ON START ++");

		connectDeviceManager();
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (settings.getBoolean(PREFS_FILESAVER, true)) {
			connectFileSaver();
		} 
		else if (mFileSaverService != null) {
			try {
				mFileSaverService.shutdown();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			disconnectFileSaver();
		}
		
		if (settings.getBoolean(PREFS_RTUPLOAD, false)) {
			connectSensorMapUploader();
		} 
		else if (mSensorMapUploaderService != null) {
			try {
				mSensorMapUploaderService.shutdown();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			disconnectSensorMapUploader();
		}

		// If BT is not on, request that it be enabled.
		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				launchRequestEnableBT();
			} 
		}
	}

	void launchRequestEnableBT() {
		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	}

	void connectDeviceManager() {
		// Start the service if not already running
		startService(new Intent(IDeviceManagerService.class.getName()));

		// Establish connection with the service.
		getApplicationContext().bindService(new Intent(IDeviceManagerService.class.getName()),
				mConnection, Context.BIND_AUTO_CREATE);
	}

	void disconnectDeviceManager() {
		if (mService != null) {
			try {
				mService.unregisterCallback(mCallback);
			} 
			catch (RemoteException e) {
				// There is nothing special we need to do if the service
				// has crashed.
				Log.e(TAG, "Exception during unregister callback.");
			}

			mService = null;

			// Detach our existing connection.
			getApplicationContext().unbindService(mConnection);
		}
	}

	void shutdown() {
		if (mService != null) {
			try {
				mService.shutdown();
			} 
			catch (RemoteException e) {
				Log.e(TAG, "Exception during shutdown device manage service");
			}
		}
		if (mSensorMapUploaderService != null) {
			try {
				mSensorMapUploaderService.shutdown();
			} 
			catch (RemoteException e) {
				Log.e(TAG, "Exception during shutdown sensor map uploader service.");
			}
		}
		if (mFileSaverService != null) {
			try {
				mFileSaverService.shutdown();
			} 
			catch (RemoteException e) {
				Log.e(TAG, "Exception during shutdown file saver service.");
			}
		}
	}

	void killDeviceManager() {
		// To kill the process hosting our service, we need to know its
		// PID.  Conveniently our service has a call that will return
		// to us that information.
		if (mService != null) {
			try {
				int pid = mService.getPid();
				// Note that, though this API allows us to request to
				// kill any process based on its PID, the kernel will
				// still impose standard restrictions on which PIDs you
				// are actually able to kill.  Typically this means only
				// the process running your application and any additional
				// processes created by that app as shown here; packages
				// sharing a common UID will also be able to kill each
				// other's processes.
				Process.killProcess(pid);
			} 
			catch (RemoteException ex) {
				// Recover gracefully from the process hosting the
				// server dying.
				// Just for purposes of the sample, put up a notification.
				Toast.makeText(this, "Remote call failed", Toast.LENGTH_SHORT).show();
			}

			mService = null;
		}
	}

	void connectSensorMapUploader() {
		// Start the service if not already running
		startService(new Intent(ISensorMapUploaderService.class.getName()));

		// Establish connection with the service.
		getApplicationContext().bindService(new Intent(ISensorMapUploaderService.class.getName()),
				mSensorMapUploaderConnection, Context.BIND_AUTO_CREATE);
	}

	void disconnectSensorMapUploader() {
		if (mSensorMapUploaderService != null) {
			mSensorMapUploaderService = null;

			// Detach our existing connection.
			getApplicationContext().unbindService(mSensorMapUploaderConnection);
		}
	}

	void connectFileSaver() {
		// Start the service if not already running
		startService(new Intent(IFileSaverService.class.getName()));

		// Establish connection with the service.
		getApplicationContext().bindService(new Intent(IFileSaverService.class.getName()),
				mFileSaverConnection, Context.BIND_AUTO_CREATE);
	}

	void disconnectFileSaver() {
		if (mFileSaverService != null) {
			mFileSaverService = null;

			// Detach our existing connection.
			getApplicationContext().unbindService(mFileSaverConnection);
		}
	}

	void killFileSaver() {
		// To kill the process hosting our service, we need to know its
		// PID.  Conveniently our service has a call that will return
		// to us that information.
		if (mFileSaverService != null) {
			try {
				int pid = mFileSaverService.getPid();
				// Note that, though this API allows us to request to
				// kill any process based on its PID, the kernel will
				// still impose standard restrictions on which PIDs you
				// are actually able to kill.  Typically this means only
				// the process running your application and any additional
				// processes created by that app as shown here; packages
				// sharing a common UID will also be able to kill each
				// other's processes.
				Process.killProcess(pid);
			} 
			catch (RemoteException ex) {
				// Recover gracefully from the process hosting the
				// server dying.
				// Just for purposes of the sample, put up a notification.
				Toast.makeText(this, "Remote call failed", Toast.LENGTH_SHORT).show();
			}

			mFileSaverService = null;
		}
	}

	void killSensorMapUploader() {
		// To kill the process hosting our service, we need to know its
		// PID.  Conveniently our service has a call that will return
		// to us that information.
		if (mSensorMapUploaderService != null) {
			try {
				int pid = mSensorMapUploaderService.getPid();
				// Note that, though this API allows us to request to
				// kill any process based on its PID, the kernel will
				// still impose standard restrictions on which PIDs you
				// are actually able to kill.  Typically this means only
				// the process running your application and any additional
				// processes created by that app as shown here; packages
				// sharing a common UID will also be able to kill each
				// other's processes.
				Process.killProcess(pid);
			} 
			catch (RemoteException ex) {
				// Recover gracefully from the process hosting the
				// server dying.
				// Just for purposes of the sample, put up a notification.
				Toast.makeText(this, "Remote call failed", Toast.LENGTH_SHORT).show();
			}

			mSensorMapUploaderService = null;
		}
	}

	void connectSensor() {
		if (mService != null) {
			if (mBluetoothAdapter != null) {
				if (mBluetoothAdapter.isEnabled()) {
					// Launch the DeviceListActivity to see devices and do scan
					Intent serverIntent = new Intent(this, DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				}
				else {
					launchRequestEnableBT();
				}
			}
			else {
				try {
					String assetName = getString(R.string.logplayer_filename);
					int messageInterval = getResources().getInteger(R.integer.logplayer_msg_interval);
					mService.connectLogplayer(assetName, messageInterval);
				}
				catch (RemoteException e) {
					// Bummer eh. Not much we can do here.
					Log.e(TAG, "Exception during connect to sensor.");
				}
			}
		}
	}

	void disconnectSensor() {
		if (mService != null) {
			try {
				mService.disconnectDevice(mConnectedDeviceName);
			}
			catch (RemoteException e) {
				// Bummer eh. Not much we can do here.
				// The user can kill the service.
				Log.e(TAG, "Exception during disconnect sensor.");
			}
		}
	}

	void connectBluetoothDevice(BluetoothDevice device) {
		if (mService != null) {
			try {
				mService.connectBluetoothDevice(device);
			}
			catch (RemoteException e) {
				// Bummer eh. Not much we can do here.
				Log.e(TAG, "Exception during connectBluetoothDevice.");
			}
		}
	}

	void connectLogplayer() {
		if (mService != null) {
			try {
				String assetName = getString(R.string.logplayer_filename);
				int messageInterval = getResources().getInteger(R.integer.logplayer_msg_interval);
				mService.connectLogplayer(assetName, messageInterval);
			}
			catch (RemoteException e) {
				// Bummer eh. Not much we can do here.
				Log.e(TAG, "Exception during connectLogplayer.");
			}
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D) Log.d(TAG, "+ ON RESUME +");
		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
	}

	@Override
	public void onDestroy() {
		disconnectDeviceManager();
		disconnectSensorMapUploader();
		disconnectFileSaver();

		super.onDestroy();
		if (D) Log.d(TAG, "--- ON DESTROY ---");
	}

	void receivedSentenceBundle(Bundle bundle) {
		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		int indexOf_dollar = line.indexOf('$'); 
		if (indexOf_dollar > -1) {
			line = line.substring(indexOf_dollar);
		}
		receivedSentenceLine(line);
	}

	abstract void receivedSentenceLine(String line);

	// The Handler that gets information back from the BluetoothSensorService
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENTENCE:
				receivedSentenceBundle((Bundle)msg.obj);
				break;
			case MessageType.SENSORCONNECTION_SUCCESS:
				setConnectedDeviceName((String)msg.obj);
				break;
			case MessageType.SENSORCONNECTION_NONE:
			case MessageType.SENSORCONNECTION_FAILED:
			case MessageType.SENSORCONNECTION_LOST:
				setConnectedDeviceName(null);
				break;
			default:
				// drop all other message types
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D) Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				// Cancel discovery because it will slow down a connection
				mBluetoothAdapter.cancelDiscovery();
				
				// Get the device MAC address
				String address = data.getExtras()
				.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				connectBluetoothDevice(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled
			} 
			else {
				// User did not enable Bluetooth or an error occured
			}
			break;
		}
	}

	void setConnectedDeviceName(String connectedDeviceName) {
		mConnectedDeviceName = connectedDeviceName;
		
		if (connectedDeviceName == null) {
			mTitle.setText(R.string.title_not_connected);
		} 
		else {
			mTitle.setText(R.string.title_connected_to);
			mTitle.append(connectedDeviceName);
		}
	}

	void startCommentActivity() {
		startActivity(new Intent(this, CommentActivity.class));
	}

	private IDeviceManagerService mService = null;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = IDeviceManagerService.Stub.asInterface(service);

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				mService.registerCallback(mCallback);
			} 
			catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
				Log.e(TAG, "Exception during register callback.");
			}

			/*
			Toast.makeText(DeviceManagerClient.this, "Remote service connected",
					Toast.LENGTH_SHORT).show();
					*/
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;

			setConnectedDeviceName(null);

			Toast.makeText(DeviceManagerClient.this, "Remote service crashed",
					Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * This implementation is used to receive callbacks from the remote
	 * service.
	 */
	private IDeviceManagerServiceCallback mCallback = new IDeviceManagerServiceCallback.Stub() {
		/**
		 * Note that IPC calls are dispatched through a thread
		 * pool running in each process, so the code executing here will
		 * NOT be running in our main thread like most other things -- so,
		 * to update the UI, we need to use a Handler to hop over there.
		 */
		public void receivedSentenceBundle(Bundle bundle) {
			mHandler.obtainMessage(MessageType.SENTENCE, bundle).sendToTarget();
		}

		public void receivedSensorConnectionNone() {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_NONE).sendToTarget();
		}

		public void receivedSensorConnectionFailed(String deviceName) {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_FAILED, deviceName).sendToTarget();
		}

		public void receivedSensorConnectionLost(String deviceName) {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_LOST, deviceName).sendToTarget();
		}

		public void receivedSensorConnectionSuccess(String deviceName) {
			mHandler.obtainMessage(MessageType.SENSORCONNECTION_SUCCESS, deviceName).sendToTarget();
		}
	};

	private ISensorMapUploaderService mSensorMapUploaderService = null;

	/**
	 * Class for interacting with the SensorMap uploader service.
	 */
	private ServiceConnection mSensorMapUploaderConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mSensorMapUploaderService = ISensorMapUploaderService.Stub.asInterface(service);
			try {
				mSensorMapUploaderService.activate();
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
				Log.e(TAG, "Exception during activate.");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mSensorMapUploaderService = null;
		}
	};	

	private IFileSaverService mFileSaverService = null;

	/**
	 * Class for interacting with the file saver service.
	 */
	private ServiceConnection mFileSaverConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service.  We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mFileSaverService = IFileSaverService.Stub.asInterface(service);
			try {
				mFileSaverService.activate();
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
				Log.e(TAG, "Exception during activate.");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mFileSaverService = null;
		}
	};
}
