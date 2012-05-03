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

package com.ecocitizen.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class HttpHelper {
	// Debugging
	private static final String TAG = "HttpUtil";
	private static final boolean D = false;

	// Constants
	public final String HTTP_USER_AGENT;
 
	public static final int HTTP_STATUS_OK = 200;
	public static final int WAITFOR_SENSORMAP_MILLIS = 1000;
	public static final int WAITFOR_SENSORMAP_RETRYCNT = 300;

	private final String SENSORMAP_REGISTER_CLIENT_URL;
	private final String SENSORMAP_LOGIN_URL;
	private final String SENSORMAP_STATUS_URL;
	private final String SENSORMAP_STARTSESSION_URL;
	private final String SENSORMAP_STORE_URL;
	private final String SENSORMAP_ENDSESSION_URL;
	private final String SENSORMAP_UPLOADFILE_URL;
	private final String SENSORMAP_API_VERSION = "4";
	
	public enum Status {
		SUCCESS,
		SERVER_UNREACHABLE,
		LOGIN_FAILED,
		STARTSESSION_FAILED,
		EXCEPTION,
		INTERRUPTED,
		CANCELLED,
	}
	
	private Status mLastStatus = Status.SUCCESS;
	
	public Status getLastStatus() {
		return mLastStatus;
	}
	
	public HttpHelper(Context context) {
		String userAgentString = null;
		try {
			PackageInfo packageInfo = 
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			userAgentString = String.format(
					"EcoCitizen-Android/%d/%s", 
					packageInfo.versionCode,
					packageInfo.versionName);
		} catch (NameNotFoundException e) {
			userAgentString = "EcoCitizen-Android/unknown";
		}
		HTTP_USER_AGENT = userAgentString;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		String username = settings.getString("username", "");
		String api_key = settings.getString("api_key", "");
		String map_server_url = settings.getString("map_server_url", "");
		map_server_url = map_server_url.replaceFirst("/*$", "");
		
		String bt_address = "";
		try {
			/*
			Log.d(TAG, "ANDROID_ID = " + Settings.System.getString(getContentResolver(), Settings.System.ANDROID_ID));
			Log.d(TAG, "MAC_ADDRESS = " + ((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress());
			 */
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			bt_address = bluetoothAdapter.getAddress();
		}
		catch (Exception e) {
		}

		SENSORMAP_REGISTER_CLIENT_URL = 
				String.format("%s/register/%s/", map_server_url, bt_address.replaceAll(":", "_"));
		SENSORMAP_LOGIN_URL = 
				String.format("%s/login/%s/%s/", map_server_url, username, api_key);
		SENSORMAP_STATUS_URL = 
				String.format("%s/status/", map_server_url);
		SENSORMAP_STARTSESSION_URL = 
				String.format("%s/startsession/%s/%s/1/", map_server_url, username, api_key);
		SENSORMAP_STORE_URL = 
				String.format("%s/store/%s/", map_server_url, username);
		SENSORMAP_ENDSESSION_URL = 
				String.format("%s/endsession/%s/", map_server_url, username);
		SENSORMAP_UPLOADFILE_URL =
				String.format("%s/uploadfile/%s/%s/", map_server_url, username, api_key);

	}
	
	public boolean isServerReachable() {
		if (getStringResponse(SENSORMAP_STATUS_URL) != null) {
			mLastStatus = Status.SUCCESS;
			return true;
		}
		else {
			mLastStatus = Status.SERVER_UNREACHABLE;
			return false;
		}
	}
	
	public boolean isLoginOK() {
		if (getStringResponse(SENSORMAP_LOGIN_URL).equals("True")) {
			mLastStatus = Status.SUCCESS;
			return true;
		}
		else {
			mLastStatus = Status.LOGIN_FAILED;
			return false;
		}
	}
	
	private String mStoreURL;
	private String mEndSessionURL;
	
	public boolean isStartSessionOK() {
		String sessionId = getStringResponse(SENSORMAP_STARTSESSION_URL);
		if (sessionId.equals("")) {
			mLastStatus = Status.STARTSESSION_FAILED;
			return false;
		}
		else {
			mLastStatus = Status.SUCCESS;
			mStoreURL = SENSORMAP_STORE_URL + sessionId + "/" 
					+ SENSORMAP_API_VERSION + "/";
			mEndSessionURL = SENSORMAP_ENDSESSION_URL + sessionId + "/";
			return true;
		}
	}
	
	public boolean sendStore(String datastr) {
		return waitForSendHttpHead(mStoreURL + datastr);
	}
	
	public void sendEndSession() {
		waitForSendHttpHead(mEndSessionURL);
	}
	
	public boolean sendUploadFile(File file) {
		String url = SENSORMAP_UPLOADFILE_URL;
		if (D) Log.d(TAG, url);
		
		HttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(url);
		request.setHeader("User-Agent", HTTP_USER_AGENT);
		MultipartEntity entity = new MultipartEntity();
		ContentBody contentBody = new FileBody(file);
		FormBodyPart bodyPart = new FormBodyPart("file", contentBody);
		entity.addPart(bodyPart);
		request.setEntity(entity);

		try {
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() == HTTP_STATUS_OK) {
				mLastStatus = Status.SUCCESS;
				return true;
			}
		} catch (ClientProtocolException e) {
			mLastStatus = Status.EXCEPTION;
			e.printStackTrace();
			Log.e(TAG, "Exception in sendUploadFile");
		} catch (IOException e) {
			mLastStatus = Status.EXCEPTION;
			e.printStackTrace();
			Log.e(TAG, "Exception in sendUploadFile");
		}
		
		return false;
	}
	
	/**
	 * Returns username and API key
	 * @return
	 */
	public String[] registerClient() {
		try {
			return getStringResponse(SENSORMAP_REGISTER_CLIENT_URL).split(",");
		}
		catch (Exception e) {
			return new String[] { "error", "error" };
		}
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
		request.setHeader("User-Agent", HTTP_USER_AGENT);

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
	
	private boolean sendHttpHead(String url) {
		if (D) Log.d(TAG, url);
		HttpClient client = new DefaultHttpClient();
		HttpHead request = new HttpHead(url);
		request.setHeader("User-Agent", HTTP_USER_AGENT);

		try {
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() == HTTP_STATUS_OK) {
				mLastStatus = Status.SUCCESS;
				return true;
			}
		} catch (ClientProtocolException e) {
			mLastStatus = Status.EXCEPTION;
			e.printStackTrace();
			Log.e(TAG, "Exception in sendHttpHead");
		} catch (IOException e) {
			mLastStatus = Status.EXCEPTION;
			e.printStackTrace();
			Log.e(TAG, "Exception in sendHttpHead");
		}
		
		return false;
	}
	
	boolean mCancelRequested = false;
	
	public void cancel() {
		mCancelRequested = true;
	}
	
	public boolean waitForSendHttpHead(String url) {
		int trycnt = 0;
		while (! sendHttpHead(url) && ! mCancelRequested) {
			try {
				Thread.sleep(WAITFOR_SENSORMAP_MILLIS);
			} 
			catch (InterruptedException e) {
			}
			if (++trycnt > WAITFOR_SENSORMAP_RETRYCNT) {
				mLastStatus = Status.INTERRUPTED;
				return false;
			}
		}
		if (mCancelRequested) {
			mLastStatus = Status.CANCELLED;
			return false;
		}
		else {
			mLastStatus = Status.SUCCESS;
			return true;
		}
	}
	
}
