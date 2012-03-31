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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.DeviceManagerServiceCallback;
import com.ecocitizen.common.MessageType;
import com.ecocitizen.common.bundlewrapper.NoteBundleWrapper;
import com.ecocitizen.common.bundlewrapper.SensorDataBundleWrapper;

public class FileSaverService extends Service {
	// Debugging
	private static final String TAG = "FileSaverService";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(FileSaverService.class);

	private boolean shouldStartSession = true;
	
	public final static String EXTERNAL_TARGETDIR = "Download";
	public final static String FILENAME_PREFIX = "EcoCitizen_";
	public final static String FILENAME_PREFIX_LEGACY = "session_";
	public final static String FILENAME_EXTENSION = "csv";
	public final static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyyMMddHHmm");

	private final static String PREFS_EXTERNAL_STORAGE = "use_external_storage";

	private OutputStream mWriter = null;

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
		super.onCreate();
		if (D) Log.d(TAG, "+++ ON CREATE +++");
	}

	@Override
	public void onDestroy() {
		if (D) Log.d(TAG, "--- ON DESTROY ---");
		disconnectDeviceManager();
		Toast.makeText(this, "File saver stopped", Toast.LENGTH_SHORT);
		super.onDestroy();
	}

	private final IFileSaverService.Stub mBinder = new IFileSaverService.Stub() {
		public void deactivate() throws RemoteException {
			FileSaverService.this.deactivate();
		}

		public void activate() throws RemoteException {
			FileSaverService.this.activate();
		}

		public int getPid() throws RemoteException {
			return Process.myPid();
		}

		public void shutdown() throws RemoteException {
			stopSelf();
		}
	};

	private void activate() {
		active = true;
		connectDeviceManager();
	}

	private void deactivate() {
		active = false;
		disconnectDeviceManager();
	}

	private void connectDeviceManager() {
		// Start the service if not already running
		startService(new Intent(IDeviceManagerService.class.getName()));
		// Establish connection with the service.
		bindService(new Intent(IDeviceManagerService.class.getName()),
				mConnection, Context.BIND_AUTO_CREATE);
	}

	private void disconnectDeviceManager() {
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
			unbindService(mConnection);
		}
	}

	private boolean active = false;

	private void receivedSensorDataBundle(Bundle bundle) {
		if (! active) return;
		
		String datarecord = new SensorDataBundleWrapper(bundle).toString();

		saveDataRecord(datarecord);
	}
	
	private void receivedNoteBundle(Bundle bundle) {
		if (! active) return;
		saveDataRecord(new NoteBundleWrapper(bundle).toString());
	}

	// The Handler that gets information back from the BluetoothSensorService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENSOR_DATA:
				if (shouldStartSession) {
					startSession();
					shouldStartSession = false;
				}
				receivedSensorDataBundle((Bundle)msg.obj);
				break;
			case MessageType.NOTE:
				if (shouldStartSession) {
					startSession();
					shouldStartSession = false;
				}
				receivedNoteBundle((Bundle)msg.obj);
				break;
			case MessageType.SM_ALL_DEVICES_GONE:
				endSession();
				shouldStartSession = true;
				break;
			default:
				// drop all other message types
				break;
			}
		}
	};

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
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
		}
	};

	/**
	 * Callback to receive calls from the remote service.
	 */
	private IDeviceManagerServiceCallback mCallback = new DeviceManagerServiceCallback(mHandler);

	private void startSession() {
		if (D) Log.d(TAG, "STARTSESSION");
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		if (settings.getBoolean(PREFS_EXTERNAL_STORAGE, false)
				&& Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			startSession_externalStorage();
		} 
		else {
			startSession_internalStorage();
		}
	}
	
	private void startSession_externalStorage() {
		String basedirectoryPath = String.format(
				"%s/%s",
				Environment.getExternalStorageDirectory().getPath(),
				EXTERNAL_TARGETDIR
		);
		File directory = new File(basedirectoryPath); 
		if (!directory.exists()) { 
			if (!directory.mkdirs()) {
				startSession_internalStorage();
				return; 
			}
		}       
		String datestr  = DATEFORMAT.format(new Date());
		String filename = String.format(
				"%s/%s%s.%s",
				basedirectoryPath,
				FILENAME_PREFIX,
				datestr,
				FILENAME_EXTENSION
		);
		try {
			mWriter = new FileOutputStream(new File(filename));
			if (D) Log.d(TAG, "STARTSESSION " + filename);
		} catch (IOException e) {
			e.printStackTrace();
			startSession_internalStorage();
		}
	}

	private void startSession_internalStorage() {
		String datestr  = DATEFORMAT.format(new Date());
		String filename = String.format(
				"%s%s.%s",
				FILENAME_PREFIX,
				datestr,
				FILENAME_EXTENSION
		);
		try {
			mWriter = openFileOutput(filename, Context.MODE_PRIVATE);
			if (D) Log.d(TAG, "STARTSESSION " + filename);
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
			stopSelf(); // the service is completely useless in this case
		}	
	}
	
	private void saveDataRecord(String data) {
		byte[] buffer = null;
		try {
			buffer = data.getBytes();
			mWriter.write(buffer);
			mWriter.write('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void endSession() {
		if (mWriter == null) return;
		if (D) Log.d(TAG, "ENDSESSION");
		
		try {
			mWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mWriter = null;
	}
}
