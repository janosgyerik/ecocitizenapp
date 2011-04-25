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

import java.io.InputStream;
import java.util.Formatter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

//import com.ecocitizen.app.R;
import com.ecocitizen.app.TreeViewActivity;
import com.ecocitizen.service.IDeviceManagerService;
import com.ecocitizen.service.IDeviceManagerServiceCallback;
import com.ecocitizen.service.ISensorMapUploaderService;

public class SensorMapUploaderService extends Service {
	// Debugging
	private static final String TAG = "SensorMapUploaderService";
	private static final boolean D = true;

	private static boolean SHOULD_CALL_STARTSESSION = false;

	private NotificationManager mNotificationManager;

	private static final int ICON_LOGINERROR = android.R.drawable.stat_sys_warning;
	private static final int ICON_STANDBY = android.R.drawable.stat_sys_phone_call_on_hold;
	private static final int ICON_UPLOADING = android.R.drawable.stat_sys_phone_call;
	private static final int ICON_BLOCKED = android.R.drawable.stat_notify_missed_call;

	// TODO switch to these when they are ready
	//	private static final int ICON_STANDBY = R.drawable.smu_standby;
	//	private static final int ICON_UPLOADING = R.drawable.smu_uploading;
	//	private static final int ICON_BLOCKED = R.drawable.smu_blocked;

	private enum Status {
		NONE,
		STANDBY,
		UPLOADING,
		BLOCKED,
		LOGINERROR
	}
	private Status status = Status.NONE;

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

		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		updateStatus(Status.STANDBY);
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

		public void shutdown() throws RemoteException {
			mNotificationManager.cancelAll();
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
		
		/* TODO
		 * Do not upload GPS sentences.
		 * This is not a very good thing to do (not clean).
		 * But, the thing is, GPS sentences are kind of useless,
		 * because GPS information is attached anyway using Android's
		 * own GPS, which in our experience so far is better than
		 * the GPS of sensors. So, these sentences are useless,
		 * and just take up unnecessary bandwidth.
		 * In the long term however, this kind of hard coding
		 * should be controllable by advanced settings screen or something.
		 */
		if (line.startsWith("$GP")) return;

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

		uploadDataRecord(datarecord);
	}

	// The Handler that gets information back from the BluetoothSensorService
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.SENTENCE:
				if (SHOULD_CALL_STARTSESSION) {
					startSession();
					SHOULD_CALL_STARTSESSION = false;
				}
				receivedSentenceBundle((Bundle)msg.obj);
				break;
			case MessageType.SM_CONNECT_FAILED:
			case MessageType.SM_CONNECTION_LOST:
			case MessageType.SM_DISCONNECTED:
				endSession();
				break;
			case MessageType.SM_CONNECTED:
				SHOULD_CALL_STARTSESSION = true;
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
			mHandler.obtainMessage(MessageType.SM_DISCONNECTED).sendToTarget();
		}

		public void receivedSensorConnectionFailed(String deviceName) {
			mHandler.obtainMessage(MessageType.SM_CONNECT_FAILED, deviceName).sendToTarget();
		}

		public void receivedSensorConnectionLost(String deviceName) {
			mHandler.obtainMessage(MessageType.SM_CONNECTION_LOST, deviceName).sendToTarget();
		}

		public void receivedSensorConnectionSuccess(String deviceName) {
			mHandler.obtainMessage(MessageType.SM_CONNECTED, deviceName).sendToTarget();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// web service handling 
	//////////////////////////////////////////////////////////////////////////

	public static final int HTTP_STATUS_OK = 200;

	public static final int WAITFOR_SENSORMAP_MILLIS   = 30000;
	public static final int WAITFOR_PREFSCHANGE_MILLIS = 10000;

	String SENSORMAP_LOGIN_URL;
	String SENSORMAP_STATUS_URL;
	String SENSORMAP_STARTSESSION_URL;
	String SENSORMAP_STORE_URL;
	String SENSORMAP_ENDSESSION_URL;
	final String SENSORMAP_API_VERSION = "4";

	// Member variables
	String username;
	String api_key;
	String map_server_url;

	private String mSessionId = null;

	void reloadConfiguration() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		username = settings.getString("username", "");
		map_server_url = settings.getString("map_server_url", "");
		api_key = settings.getString("api_key", "");

		SENSORMAP_LOGIN_URL = String.format("%slogin/%s/%s", map_server_url, username, api_key);
		SENSORMAP_STATUS_URL = String.format("%sstatus/", map_server_url);
		SENSORMAP_STARTSESSION_URL = String.format("%sstartsession/%s/%s/", map_server_url, username, api_key);
		SENSORMAP_STORE_URL = String.format("%sstore/%s/", map_server_url, username);
		SENSORMAP_ENDSESSION_URL = String.format("%sendsession/%s/", map_server_url, username);
	}

	void startSession() {
		if (D) Log.d(TAG, "STARTSESSION");
		mSessionId = null;
		waitForSensorMapReachable();
		waitForStartSession();
	}

	void uploadDataRecord(String data) {
		//if (D) Log.d(TAG, "STORE = " + data);
		waitForStore(data);
	}

	void endSession() {
		if (D) Log.d(TAG, "ENDSESSION");
		if (mSessionId != null) waitForEndSession();
	}

	/**
	 * Returns HTTP response as string, or NULL on network errors
	 * and if response status code is not HTTP_STATUS_OK.
	 * 
	 * @param url
	 * @return
	 */
	String getStringResponse(String url) {
		if (D) Log.d(TAG, url);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);

		try {
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) return null;

			HttpEntity entity = response.getEntity();
			InputStream istream = entity.getContent();
			byte[] buf = new byte[100];
			int bytes_read = istream.read(buf);
			if (bytes_read > 0) {
				char[] charbuf = new char[bytes_read];
				for (int i = 0; i < bytes_read; ++i) charbuf[i] = (char)buf[i];
				String str = String.valueOf(charbuf);
				return str;
			}
			else {
				return "";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Exception in getStringResponse");
			return null;
		}
	}

	void waitForSensorMapReachable() {
		while (active) {
			if (mSessionId == null) reloadConfiguration();

			String ret = getStringResponse(SENSORMAP_STATUS_URL);
			if (ret != null) { 
				return;
			}
			else {
				Log.e(TAG, "sensormap UNREACHABLE");
				updateStatus(Status.BLOCKED);
				try {
					Thread.sleep(WAITFOR_SENSORMAP_MILLIS);
				} catch (InterruptedException e) {
					// ignore sleep interrupts
				}
			}
		}
	}

	void waitForAccountInfoChange() {
		updateStatus(Status.LOGINERROR);

		String old_username = username;
		String old_map_server_url = map_server_url;
		String old_api_key = api_key;

		while (active) {
			if (D) Log.d(TAG, "Waiting for change in preferences ...");
			reloadConfiguration();

			if (!old_username.equals(username) 
					|| !old_map_server_url.equals(map_server_url)
					|| !old_api_key.equals(api_key)) {
				if (D) Log.d(TAG, "OK, change in preferences detected.");
				return;
			}

			try {
				Thread.sleep(WAITFOR_PREFSCHANGE_MILLIS);
			} catch (InterruptedException e) {
				// ignore sleep interrupts
			}
		}
	}

	void waitForStartSession() {
		while (active) {
			mSessionId = getStringResponse(SENSORMAP_STARTSESSION_URL);
			if (mSessionId == null) {
				// network error
				Log.e(TAG, "network error during /startsession, sensor map UNREACHABLE");
				waitForSensorMapReachable();
			}
			else if (mSessionId.equals("")) {
				// login error
				Log.e(TAG, "login error during /startsession, fix account info in preferences");
				waitForAccountInfoChange();
			}
			else {
				// success!
				return;
			}
		}
	}

	void waitForStore(String data) {
		data = data.replace(" ", "");
		while (active) {
			String ret = getStringResponse(SENSORMAP_STORE_URL + mSessionId + "/" 
					+ SENSORMAP_API_VERSION + "/" + data); 
			//URLEncoder.encode(data));
			if (ret == null) {
				// network error
				Log.e(TAG, "network error during /store, sensor map UNREACHABLE");
				waitForSensorMapReachable();
			}
			else {
				// success!
				updateStatus(Status.UPLOADING);
				return;
			}
		}
	}

	void waitForEndSession() {
		while (active) {
			String ret = getStringResponse(SENSORMAP_ENDSESSION_URL + mSessionId + "/"); 
			if (ret == null) {
				// network error
				Log.e(TAG, "network error during /endsession, sensor map UNREACHABLE");
				waitForSensorMapReachable();
			}
			else {
				// success!
				mSessionId = null;
				updateStatus(Status.STANDBY);
				return;
			}
		}
	}

	void updateStatus(Status status) {
		if (this.status == status) return;
		this.status = status;

		Context context = getApplicationContext();
		CharSequence contentTitle = context.getString(com.ecocitizen.app.R.string.notification_smu_title);
		CharSequence tickerText = context.getString(com.ecocitizen.app.R.string.notification_smu_ticker);

		Notification notification;
		long when = System.currentTimeMillis();
		int icon = 0;
		CharSequence contentText = null;

		switch (status) {
		case LOGINERROR:
			icon = ICON_LOGINERROR;
			contentText = context.getString(com.ecocitizen.app.R.string.notification_smu_loginerror);
			break;
		case STANDBY:
			icon = ICON_STANDBY;
			contentText = context.getString(com.ecocitizen.app.R.string.notification_smu_standby);
			break;
		case UPLOADING:
			icon = ICON_UPLOADING;
			contentText = context.getString(com.ecocitizen.app.R.string.notification_smu_uploading);
			break;
		case BLOCKED:
		default:
			icon = ICON_BLOCKED;
			contentText = context.getString(com.ecocitizen.app.R.string.notification_smu_blocked);
			break;
		}
		notification = new Notification(icon, tickerText, when);
		Intent notificationIntent = new Intent(this, TreeViewActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(1, notification);
	}
}
