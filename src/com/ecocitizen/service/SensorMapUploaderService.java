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

import java.io.InputStream;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ecocitizen.app.TreeViewActivity;
import com.ecocitizen.common.DeviceManagerServiceCallback;
import com.ecocitizen.common.MessageType;
import com.ecocitizen.common.bundlewrapper.NoteBundleWrapper;
import com.ecocitizen.common.bundlewrapper.SensorDataBundleWrapper;

public class SensorMapUploaderService extends Service {
	// Debugging
	private static final String TAG = "SensorMapUploaderService";
	private static final boolean D = false;

	private boolean shouldStartSession = true;

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

	private void activate() {
		active = true;

		connectDeviceManager();
	}

	private void deactivate() {
		active = false;

		disconnectDeviceManager();

		// TODO cancel pending SENSOR_DATA messages
		// TODO do not cancel other messages, we still need them to track sessions
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

		uploadDataRecord(datarecord);
	}

	private void receivedNoteBundle(Bundle bundle) {
		if (! active) return;
		uploadDataRecord(new NoteBundleWrapper(bundle).toString());
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

	//////////////////////////////////////////////////////////////////////////
	// web service handling 
	//////////////////////////////////////////////////////////////////////////

	private static final int HTTP_STATUS_OK = 200;

	private static final int WAITFOR_SENSORMAP_MILLIS   = 30000;
	private static final int WAITFOR_PREFSCHANGE_MILLIS = 10000;

	private String SENSORMAP_STATUS_URL;
	private String SENSORMAP_STARTSESSION_URL;
	private String SENSORMAP_STORE_URL;
	private String SENSORMAP_ENDSESSION_URL;
	private final String SENSORMAP_API_VERSION = "4";

	// Member variables
	private String username;
	private String api_key;
	private String map_server_url;

	private String mSessionId = null;

	private void reloadConfiguration() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		username = settings.getString("username", "");
		map_server_url = settings.getString("map_server_url", "");
		api_key = settings.getString("api_key", "");

		SENSORMAP_STATUS_URL = String.format("%sstatus/", map_server_url);
		SENSORMAP_STARTSESSION_URL = String.format("%sstartsession/%s/%s/", map_server_url, username, api_key);
		SENSORMAP_STORE_URL = String.format("%sstore/%s/", map_server_url, username);
		SENSORMAP_ENDSESSION_URL = String.format("%sendsession/%s/", map_server_url, username);
	}

	private void startSession() {
		if (D) Log.d(TAG, "STARTSESSION");
		mSessionId = null;
		waitForSensorMapReachable();
		waitForStartSession();
	}

	private void uploadDataRecord(String data) {
		//if (D) Log.d(TAG, "STORE = " + data);
		waitForStore(data);
	}

	private void endSession() {
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
	private String getStringResponse(String url) {
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

	private void waitForSensorMapReachable() {
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

	private void waitForAccountInfoChange() {
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

	private void waitForStartSession() {
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

	private void waitForStore(String data) {
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

	private void waitForEndSession() {
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

	private void updateStatus(Status status) {
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
