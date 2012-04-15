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

package com.ecocitizen.app;

import java.util.HashMap;
import java.util.Map;

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
import android.widget.Toast;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;

import com.ecocitizen.common.DeviceManagerServiceCallback;
import com.ecocitizen.common.MessageType;
import com.ecocitizen.common.bundlewrapper.NoteBundleWrapper;
import com.ecocitizen.common.bundlewrapper.SensorDataBundleWrapper;
import com.ecocitizen.common.bundlewrapper.SensorInfoBundleWrapper;
import com.ecocitizen.service.IDeviceManagerService;
import com.ecocitizen.service.IDeviceManagerServiceCallback;
import com.ecocitizen.service.IFileSaverService;
import com.ecocitizen.service.ISensorMapUploaderService;

public abstract class DeviceManagerClient extends Activity {
	// Debugging
	private static final String TAG = "DeviceManagerClient";
	private static final boolean D = false;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_DISCONNECT_DEVICE = 3;

	private final static String PREFS_RTUPLOAD = "rtupload";
	private final static String PREFS_FILESAVER = "filesaver";

	// Local Bluetooth adapter
	BluetoothAdapter mBluetoothAdapter = null;
	
	Map<String, SensorInfoBundleWrapper> mConnectedDevices = 
		new HashMap<String, SensorInfoBundleWrapper>();

	/**
	 * The name of the most recently connected sensor.
	 * If it gets disconnected, it will take the name of
	 * one of the still connected sensors, or null.
	 */
	protected String mConnectedSensorName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+++ ON CREATE +++");

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
		}
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
					launchDeviceListActivity();
				}
				else {
					launchRequestEnableBT();
				}
			}
			else {
				launchDeviceListActivity();
			}
		}
	}
	
	private void launchDeviceListActivity() {
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	void disconnectSensor() {
		if (mService != null) {
			try {
				Bundle[] devices = mService.getConnectedDevices();
				
				if (devices.length > 1) {
					String deviceNames[] = new String[devices.length];
					String deviceIds[] = new String[devices.length];
					
					for (int i = 0; i < devices.length; ++i) {
						SensorInfoBundleWrapper sensorInfo = new SensorInfoBundleWrapper(devices[i]);
						deviceNames[i] = sensorInfo.getSensorName();
						deviceIds[i] = sensorInfo.getSensorId();
					}
					
					Intent serverIntent = new Intent(this, DisconnectDeviceActivity.class);
					serverIntent.putExtra("device_names", deviceNames);
					serverIntent.putExtra("device_ids", deviceIds);
					startActivityForResult(serverIntent, REQUEST_DISCONNECT_DEVICE);
				}
				else {
					disconnectSensor(null);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	void disconnectSensor(String deviceName) {
		if (mService != null) {
			try {
				mService.disconnectDevice(deviceName);
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

	void connectLogplayer(String filename) {
		if (mService != null) {
			try {
				int messageInterval = getResources().getInteger(R.integer.logplayer_msg_interval);
				mService.connectLogplayer(filename, messageInterval);
			}
			catch (RemoteException e) {
				// Bummer eh. Not much we can do here.
				Log.e(TAG, "Exception during connectLogplayer.");
			}
		}
	}

	void connectDummyDevice() {
		if (mService != null) {
			try {
				int messageInterval = getResources().getInteger(R.integer.dummydevice_msg_interval);
				mService.connectDummyDevice(messageInterval);
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

	abstract void receivedSensorDataBundle(SensorDataBundleWrapper bundle);
	
	void receivedNoteBundle(NoteBundleWrapper bundle) {
		Log.i(TAG, "receivedNoteBundle = " + bundle);
	}

	// The Handler that gets information back from the DeviceManagerService
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what != MessageType.SENSOR_DATA) {
				if (D) Log.d(TAG, "handleMessage " + msg.what + " " + msg.obj);
			}
			switch (msg.what) {
			case MessageType.SENSOR_DATA:
				receivedSensorDataBundle(new SensorDataBundleWrapper((Bundle)msg.obj));
				break;
			case MessageType.NOTE:
				receivedNoteBundle(new NoteBundleWrapper((Bundle)msg.obj));
				break;
			case MessageType.SM_DEVICE_ADDED:
				addConnectedDevice(new SensorInfoBundleWrapper((Bundle)msg.obj));
				break;
			case MessageType.SM_DEVICE_CLOSED:
			case MessageType.SM_DEVICE_LOST:
				removeConnectedDevice((String)msg.obj);
				break;
			case MessageType.SM_CONNECTION_FAILED:
				Toast.makeText(DeviceManagerClient.this, "Could not connect to sensor", Toast.LENGTH_LONG).show();
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
				if (mBluetoothAdapter != null) {
					// Cancel discovery because it will slow down a connection
					mBluetoothAdapter.cancelDiscovery();
				}
				
				String extra;
				
				if ((extra = data.getExtras().getString(DeviceListActivity.EXTRA_BLUETOOTH_MAC)) != null) {
					// We have a Bluetooth device MAC address
					BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(extra);
					connectBluetoothDevice(device);
				}
				else if ((extra = data.getExtras().getString(DeviceListActivity.EXTRA_LOGFILENAME)) != null) {
					// We have a logfile name
					connectLogplayer(extra);
				}
				else if ((extra = data.getExtras().getString(DeviceListActivity.EXTRA_DUMMY)) != null) {
					// We have a dummy device
					connectDummyDevice();
				}
			}
			break;
		case REQUEST_DISCONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				Bundle extras = data.getExtras();
				
				if (extras.getString(DisconnectDeviceActivity.DISCONNECT_ALL) != null) {
					try {
						Bundle[] devices = mService.getConnectedDevices();
						for (int i = 0; i < devices.length; ++i) {
							disconnectSensor(null);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				else {
					disconnectSensor(extras.getString(DisconnectDeviceActivity.DEVICE_ID));
				}
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

	void addConnectedDevice(SensorInfoBundleWrapper sensorInfo) {
		Toast.makeText(this, "Connected to " + sensorInfo.getSensorName(), Toast.LENGTH_LONG).show();
		mConnectedDevices.put(sensorInfo.getSensorId(), sensorInfo);
		mConnectedSensorName = sensorInfo.getSensorName();
		if (mConnectedDevices.size() > 1) {
			mConnectedSensorName += " [..]";
		}
		onConnectedDevicesUpdated();
	}
	
	void removeConnectedDevice(String sensorId) {
		try {
			String sensorName = mConnectedDevices.get(sensorId).getSensorName();
			Toast.makeText(this, "Disconnected from " + sensorName, Toast.LENGTH_LONG).show();
		}
		catch (Exception e) {
			Toast.makeText(this, "Disconnected from sensor", Toast.LENGTH_LONG).show();
		}
		mConnectedDevices.remove(sensorId);
		if (! mConnectedDevices.isEmpty()) {
			mConnectedSensorName = mConnectedDevices.values().iterator().next().getSensorName();
			if (mConnectedDevices.size() > 1) {
				mConnectedSensorName += " [..]";
			}
		}
		else {
			mConnectedSensorName = "";
		}
		onConnectedDevicesUpdated();
	}
	
	void clearConnectedDevices() {
		mConnectedDevices.clear();
		mConnectedSensorName = "";
		onConnectedDevicesUpdated();
	}
	
	void onConnectedDevicesUpdated() {
	}
	
	void startAddNoteActivity() {
		startActivity(new Intent(this, AddNoteActivity.class));
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

			if (mService != null) {
				try {
					Bundle[] devices = mService.getConnectedDevices();
					for (int i = 0; i < devices.length; ++i) {
						addConnectedDevice(new SensorInfoBundleWrapper(devices[i]));
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;

			clearConnectedDevices();

			Toast.makeText(DeviceManagerClient.this, "Remote service crashed",
					Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * Callback to receive calls from the remote service.
	 */
	private IDeviceManagerServiceCallback mCallback = new DeviceManagerServiceCallback(mHandler);

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
