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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import com.ecocitizen.service.IDeviceManagerService;
import com.ecocitizen.service.IDeviceManagerServiceCallback;
import com.ecocitizen.service.IFileSaverService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
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

public class FileSaverService extends Service {
	// Debugging
	private static final String TAG = "FileSaverService";
	private static final boolean D = true;

	private static boolean WAS_STARTSESSION = false;
	
	public final static String EXTERNAL_TARGETDIR = "Download";
	public final static String FILENAME_PREFIX = "session_";
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

	void activate() {
		active = true;
		connectDeviceManager();
	}

	void deactivate() {
		active = false;
		disconnectDeviceManager();
	}

	void connectDeviceManager() {
		// Start the service if not already running
		startService(new Intent(IDeviceManagerService.class.getName()));
		// Establish connection with the service.
		bindService(new Intent(IDeviceManagerService.class.getName()),
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
			unbindService(mConnection);
		}
	}

	boolean active = false;

	void receivedSentenceBundle(Bundle bundle) {
		if (! active) return;
		
		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		int indexOf_dollar = line.indexOf('$'); 
		if (indexOf_dollar > -1) {
			line = line.substring(indexOf_dollar);
		}

		Formatter formatter = new Formatter();
		String datarecord;
		Bundle locationBundle = bundle.getBundle(BundleKeys.LOCATION_BUNDLE);
		if (locationBundle == null) {
			String format = "SENTENCE,%s,%s,%s,_";
			datarecord = formatter.format(
					format,
					bundle.getString(BundleKeys.SENTENCE_SENSOR_ID),
					bundle.getString(BundleKeys.SENTENCE_DTZ),
					line
			).toString();
		}
		else {
			String format = "GPS,%s,%d,%f,%f,AndroidGps,%f,%f,%f,%f,_,SENTENCE,%s,%s,%s,_";
			Location location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
			datarecord = formatter.format(
					format,
					locationBundle.getString(BundleKeys.LOCATION_DTZ),
					locationBundle.getInt(BundleKeys.LOCATION_LATLON_ID),
					location.getLatitude(),
					location.getLongitude(),
					location.getAccuracy(),
					location.getAltitude(),
					location.getBearing(),
					location.getSpeed(),
					bundle.getString(BundleKeys.SENTENCE_SENSOR_ID),
					bundle.getString(BundleKeys.SENTENCE_DTZ),
					line
			).toString();
		}
		saveDataRecord(datarecord);
	}

	// The Handler that gets information back from the BluetoothSensorService
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENTENCE:
				if (! WAS_STARTSESSION) {
					startSession();
					WAS_STARTSESSION = true;
				}
				receivedSentenceBundle((Bundle)msg.obj);
				break;
			case MessageType.SENSORCONNECTION_FAILED:
			case MessageType.SENSORCONNECTION_LOST:
			case MessageType.SENSORCONNECTION_NONE:
				endSession();
				break;
			case MessageType.SENSORCONNECTION_SUCCESS:
				WAS_STARTSESSION = false;
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

	void startSession() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		if (settings.getBoolean(PREFS_EXTERNAL_STORAGE, false)
				&& Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			startSession_externalStorage();
		} 
		else {
			startSession_internalStorage();
		}
	}
	
	void startSession_externalStorage() {
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
		} catch (IOException e) {
			e.printStackTrace();
			startSession_internalStorage();
		}
	}

	void startSession_internalStorage() {
		String datestr  = DATEFORMAT.format(new Date());
		String filename = String.format(
				"%s%s.%s",
				FILENAME_PREFIX,
				datestr,
				FILENAME_EXTENSION
		);
		try {
			mWriter = openFileOutput(filename, Context.MODE_PRIVATE);
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
			stopSelf(); // the service is completely useless in this case
		}	
	}
	
	void saveDataRecord(String data) {
		byte[] buffer = null;
		try {
			buffer = data.getBytes();
			mWriter.write(buffer);
			mWriter.write('\n');
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void endSession() {
		if (D) Log.d(TAG, "ENDSESSION");
		
		try {
			mWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mWriter = null;
	}
}
