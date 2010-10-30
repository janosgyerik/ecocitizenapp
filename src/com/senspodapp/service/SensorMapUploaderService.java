/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
 *
 * This file is part of SenspodApp.
 *
 * SenspodApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SenspodApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SenspodApp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.senspodapp.service;

import java.io.InputStream;
import java.util.Formatter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SensorMapUploaderService extends Service {
	// Debugging
	private static final String TAG = "SensorMapUploaderService";
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
		super.onCreate();
		if (D) Log.d(TAG, "+++ ON CREATE +++");
	}

	@Override
	public void onDestroy() {
		if (D) Log.d(TAG, "--- ON DESTROY ---");

		disconnectDeviceManager();
		
		Toast.makeText(this, "Sensor Map Uploader stopped", Toast.LENGTH_SHORT);
		
		super.onDestroy();
	}
	
	private final ISensorMapUploaderService.Stub mBinder = new ISensorMapUploaderService.Stub() {
		public void deactivate() throws RemoteException {
			SensorMapUploaderService.this.deactivate();
		}
		
		public void activate() throws RemoteException {
			SensorMapUploaderService.this.activate();
		}
		
		public int getPid() throws RemoteException {
			return Process.myPid();
		}
	};
	
	void activate() {
		active = true;
		
		connectDeviceManager();
	}
	
	void deactivate() {
		active = false;
		
		disconnectDeviceManager();
		
		// TODO cancel pending SENTENCE messages
		// TODO do not cancel other messages, we still need them to track sessions
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
					bundle.getString(BundleKeys.SENTENCE_LINE)
			).toString();
		}
		else {
			String format = "GPS,%s,%d,%f,%f,AndroidGps,%f,%f,%f,%f,_,SENTENCE,%s,%s,%s,_";
			Location location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
			datarecord = formatter.format(
					format,
					locationBundle.getString(BundleKeys.LOCATION_DTZ),
					locationBundle.getString(BundleKeys.LOCATION_LATLON_ID),
					location.getLatitude(),
					location.getLongitude(),
					location.getAccuracy(),
					location.getAltitude(),
					location.getBearing(),
					location.getSpeed(),
					bundle.getString(BundleKeys.SENTENCE_SENSOR_ID),
					bundle.getString(BundleKeys.SENTENCE_DTZ),
					bundle.getString(BundleKeys.SENTENCE_LINE)
			).toString();
		}
		
		uploadDataRecord(datarecord);
	}
	
	// The Handler that gets information back from the BluetoothSensorService
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENTENCE:
				receivedSentenceBundle((Bundle)msg.obj);
				break;
			case MessageType.SENSORCONNECTION_FAILED:
			case MessageType.SENSORCONNECTION_LOST:
			case MessageType.SENSORCONNECTION_NONE:
				endSession();
				break;
			case MessageType.SENSORCONNECTION_SUCCESS:
				startSession();
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
	
	//////////////////////////////////////////////////////////////////////////
	// web service handling 
	//////////////////////////////////////////////////////////////////////////

    public static final int HTTP_STATUS_OK = 200;
	public static final int QUEUE_NOSENSORMAP_SLEEP = 30000;
	public static final int QUEUE_LOGINERROR_SLEEP = 10000;
	public static final int QUEUE_STOREERROR_SLEEP = 10000;
	
	String SENSORMAP_LOGIN_URL;
	String SENSORMAP_STATUS_URL;
	String SENSORMAP_STARTSESSION_URL;
	String SENSORMAP_STORE_URL;
	String SENSORMAP_ENDSESSION_URL;
	String API_VERSION = "4";
	
	// Member variables
	String username;
	String api_key;
	String map_server_url;
	
	private int mSessionId = 0;
	
	void uploadDataRecord(String data) {
		Log.d(TAG, "STORE = " + data);
		waitForStore(data);
	}
	
	void startSession() {
		Log.d(TAG, "STARTSESSION");
		reloadConfiguration();
		waitForSensorMapReachable();
		waitForStartSession();
	}
	
	void endSession() {
		Log.d(TAG, "ENDSESSION");
		ws_endsession();
	}

	void reloadConfiguration() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		username = settings.getString("username", "");
		api_key = settings.getString("api_key", "");
		map_server_url = settings.getString("map_server_url", "");

		SENSORMAP_LOGIN_URL = String.format("%slogin/%s/%s", map_server_url, username, api_key);
		SENSORMAP_STATUS_URL = String.format("%sstatus/", map_server_url);
		SENSORMAP_STARTSESSION_URL = String.format("%sstartsession/%s/%s", map_server_url, username, api_key);
		SENSORMAP_STORE_URL = String.format("%sstore/%s/%s/", map_server_url, API_VERSION, username);
		SENSORMAP_ENDSESSION_URL = String.format("%sendsession/%s/", map_server_url, username);
	}
	
	public boolean isSensormapReachable() {
		if (D) Log.d(TAG, SENSORMAP_STATUS_URL);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(SENSORMAP_STATUS_URL);
        
        try {
        	HttpResponse response = client.execute(request);
        	StatusLine status = response.getStatusLine();
        	return status.getStatusCode() == HTTP_STATUS_OK;
        }
        catch (Exception e) {
        	//e.printStackTrace();
        	Log.e(TAG, "Exception in isSensormapReachable");
        	return false;
        }
	}
	
	public int getIntResponse(String url) {
		if (D) Log.d(TAG, url);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        
        try {
         	HttpResponse response = client.execute(request);
        	StatusLine status = response.getStatusLine();
        	if (status.getStatusCode() != HTTP_STATUS_OK) return -1;

        	HttpEntity entity = response.getEntity();
			InputStream istream = entity.getContent();
			byte[] buf = new byte[100];
			int bytes_read = istream.read(buf);
			char[] charbuf = new char[bytes_read];
			for (int i = 0; i < bytes_read; ++i) charbuf[i] = (char)buf[i];
			String intstr = String.valueOf(charbuf);
			return Integer.valueOf(intstr);
        }
        catch (Exception e) {
        	//e.printStackTrace();
        	Log.e(TAG, "Exception in getIntResponse");
        	return -1;
        }
	}
	
	public void waitForSensorMapReachable() {
		while (active) {
			if (isSensormapReachable()) {
				return;
			}
			else {
				Log.e(TAG, "sensormap UNREACHABLE");
				try {
					Thread.sleep(QUEUE_NOSENSORMAP_SLEEP);
				} catch (InterruptedException e) {
					// ignore sleep interrupts
				}
			}
		}
	}
	
	public void waitForStartSession() {
		while (active) {
			if (ws_startsession(username)) {
				return;
			}
			else {
				Log.e(TAG, "startsession ERROR");
				try {
					Thread.sleep(QUEUE_LOGINERROR_SLEEP);
				} catch (InterruptedException e) {
					// ignore sleep interrupts
				}
			}
		}
	}
	
	public void waitForStore(String data) {
		while (active) {
			if (ws_store(data)) {
				return;
			}
			else {
				Log.e(TAG, "store ERROR");
				try {
					Thread.sleep(QUEUE_STOREERROR_SLEEP);
				} catch (InterruptedException e) {
					// ignore sleep interrupts
				}
			}
		}
	}
	
	public boolean ws_startsession(String username) {
		mSessionId = getIntResponse(SENSORMAP_STARTSESSION_URL);
		return mSessionId > 0;
	}
	
	public boolean ws_store(String data) {
		data = data.replace(" ", "");
		int ret = getIntResponse(SENSORMAP_STORE_URL + mSessionId + "/" + data); //URLEncoder.encode(data));
		return ret == 0;
	}
	
	public void ws_endsession() {
		getIntResponse(SENSORMAP_ENDSESSION_URL + mSessionId);
	}

}