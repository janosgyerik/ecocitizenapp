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

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.SharedPreferences;
import android.util.Log;
import backport.android.bluetooth.BluetoothAdapter;

public class HttpHelper {
	// Debugging
	private static final String TAG = "HttpUtil";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(HttpHelper.class);

	// Constants
	public final String HTTP_USER_AGENT;
 
	public static final int HTTP_STATUS_OK = 200;
	public static final int WAITFOR_SENSORMAP_MILLIS = 1000;
	public static final int WAITFOR_SENSORMAP_RETRYCNT = 300;

	private final String SENSORMAP_REGISTER_CLIENT_URL;
	private final String SENSORMAP_LOGIN_URL;
	private final String SENSORMAP_STATUS_URL;

	public HttpHelper(SharedPreferences settings, String userAgentString) {
		String username = settings.getString("username", "");
		String map_server_url = settings.getString("map_server_url", "");
		String api_key = settings.getString("api_key", "");
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

		SENSORMAP_REGISTER_CLIENT_URL = String.format("%sregister/%s/", map_server_url, bt_address);
		SENSORMAP_LOGIN_URL = String.format("%slogin/%s/%s/", map_server_url, username, api_key);
		SENSORMAP_STATUS_URL = String.format("%sstatus/", map_server_url);
		
		HTTP_USER_AGENT = userAgentString;
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
	
	public enum Status {
		SUCCESS,
		SERVER_UNREACHABLE,
		LOGIN_FAILED,
		EXCEPTION,
		STARTSESSION_FAILED,
		EMPTY_FILE,
		UPLOAD_INTERRUPTED,
		UPLOAD_CANCELLED
	}
	
	public boolean isServerReachable() {
		return getStringResponse(SENSORMAP_STATUS_URL) != null;
	}
	
	public boolean isLoginOK() {
		return getStringResponse(SENSORMAP_LOGIN_URL).equals("True");
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
}
