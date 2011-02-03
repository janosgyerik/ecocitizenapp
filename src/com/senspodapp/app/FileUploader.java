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

package com.senspodapp.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.SharedPreferences;
import android.util.Log;

public class FileUploader {
	// Debugging
	private static final String TAG = "FileUploader";
	private static final boolean D = true;

	// Constants

	public static final int HTTP_STATUS_OK = 200;
	public static final int WAITFOR_SENSORMAP_MILLIS = 1000;
	public static final int WAITFOR_SENSORMAP_RETRYCNT = 300;

	private final String SENSORMAP_LOGIN_URL;
	private final String SENSORMAP_STATUS_URL;
	private final String SENSORMAP_STARTSESSION_URL;
	private final String SENSORMAP_STORE_URL;
	private final String SENSORMAP_ENDSESSION_URL;
	private final String SENSORMAP_API_VERSION = "4";

	// Member variables
	private File mFile;
	private String username;
	private String api_key;
	private String map_server_url;

	public FileUploader(SharedPreferences settings, File file) {
		mFile = file;
		
		username = settings.getString("username", "");
		map_server_url = settings.getString("map_server_url", "");
		api_key = settings.getString("api_key", "");

		SENSORMAP_LOGIN_URL = String.format("%slogin/%s/%s/", map_server_url, username, api_key);
		SENSORMAP_STATUS_URL = String.format("%sstatus/", map_server_url);
		SENSORMAP_STARTSESSION_URL = String.format("%sstartsession/%s/%s/1/", map_server_url, username, api_key);
		SENSORMAP_STORE_URL = String.format("%sstore/%s/", map_server_url, username);
		SENSORMAP_ENDSESSION_URL = String.format("%sendsession/%s/", map_server_url, username);
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
	
	public boolean waitForStringResponse(String url) {
		int trycnt = 0;
		while (getStringResponse(url) == null) {
			try {
				Thread.sleep(WAITFOR_SENSORMAP_MILLIS);
			} 
			catch (InterruptedException e) {
			}
			if (++trycnt > WAITFOR_SENSORMAP_RETRYCNT) return false;
		}
		return true;
	}
	
	boolean waitForStringResponse(String storeBaseURL, String line) {
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
		if (line.indexOf(",$GP") > -1) return true;
		
		return waitForStringResponse(storeBaseURL + line);
	}

	public enum Status {
		SUCCESS,
		SERVER_UNREACHABLE,
		LOGIN_FAILED,
		EXCEPTION,
		STARTSESSION_FAILED,
		EMPTY_FILE,
		UPLOAD_INTERRUPTED
	}
	
	public boolean isServerReachable() {
		return getStringResponse(SENSORMAP_STATUS_URL) != null;
	}
	
	public boolean isLoginOK() {
		return getStringResponse(SENSORMAP_LOGIN_URL).equals("True");
	}
	
	public Status upload() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));
			try {
				String sessionId;
				String storeBaseURL; 
				
				String line = reader.readLine();
				if (line != null) {
					if (!isServerReachable()) return Status.SERVER_UNREACHABLE; 
					
					if (!isLoginOK()) return Status.LOGIN_FAILED;
					
					sessionId = getStringResponse(SENSORMAP_STARTSESSION_URL);
					if (sessionId.equals("")) return Status.STARTSESSION_FAILED;

					storeBaseURL = SENSORMAP_STORE_URL + sessionId + "/" 
						+ SENSORMAP_API_VERSION + "/";
				}
				else {
					return Status.EMPTY_FILE;
				}
				do {
					line = line.replace(" ", "");
					// TODO: clean this up, after android is cleaned up
					if (line.matches(".*,_[SG].*")) {
						Log.d(TAG, "Applying workaround for FileSaver bug. To be deprecated soon.");
						int pos = 0;
						int start = -1;
						while ((start = line.indexOf("_S", pos)) > -1 || (start = line.indexOf("_G", pos)) > -1) {
							if (start + 1 < line.length() && line.charAt(start + 1) == ',') {
								continue;
							}
							String newline = line.substring(pos, start + 1);
							
							if (!waitForStringResponse(storeBaseURL, newline)) {
								return Status.UPLOAD_INTERRUPTED;
							}
							pos = start + 1;
						}
						break;
					}
					else {
						if (!waitForStringResponse(storeBaseURL, line)) {
							return Status.UPLOAD_INTERRUPTED;
						}
					}
				}
				while ((line = reader.readLine()) != null);
				
				waitForStringResponse(SENSORMAP_ENDSESSION_URL + sessionId + "/");
			} 
			catch (IOException e) {
				e.printStackTrace();
				return Status.EXCEPTION;
			}
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return Status.EXCEPTION;
		}

		return Status.SUCCESS;
	}
}
