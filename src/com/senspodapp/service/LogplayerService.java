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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LogplayerService extends SensorManager {
	// Debugging
	static final String TAG = "LogplayerService";
	static final boolean D = true;

	// Member fields
	private InputStream mmInStream = null;
	private ConnectedThread mConnectedThread;

	private final int mMessageInterval;

	/**
	 * Constructor. Prepares a new session.
	 * @param handler  A Handler to send messages back to the UI Activity
	 */
	public LogplayerService(Handler handler, GpsLocationListener gpsLocationListener,
			InputStream instream, int messageInterval, String filename) {
		mHandler = handler;
		mGpsLocationListener = gpsLocationListener;

		mMessageInterval = messageInterval;
		mmInStream = instream;

		mSensorId = filename;
		mDeviceName = filename;
	}

	@Override
	public synchronized void start() {
		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread();
		mConnectedThread.start();

		sendToHandler(MessageType.SENSORCONNECTION_SUCCESS);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D) Log.d(TAG, "stop");
		if (mConnectedThread != null) mConnectedThread.shutdown();
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		sendToHandler(MessageType.SENSORCONNECTION_NONE);
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming transmissions.
	 */
	private class ConnectedThread extends Thread {
		private boolean stop = false;
		private boolean hasReadAnything = false;

		public ConnectedThread() {
			Log.d(TAG, "create ConnectedThread");
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");

			BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));

			while (! stop) {
				try {
					String line = reader.readLine();
					if (line != null) {
						hasReadAnything = true;
						Bundle bundle = getSensorDataBundle(line);
						mHandler.obtainMessage(MessageType.SENTENCE, bundle).sendToTarget();
						try {
							Thread.sleep(mMessageInterval);
						}
						catch (InterruptedException e) {
						}
					}
					else {
						connectionNone();
					}
				} 
				catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;			
				}
				catch (Exception e) {
					// Sometimes, NullPointerException can happen during shutdown...
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;			
					/*
					 * E/AndroidRuntime(  957): java.lang.NullPointerException
					 * E/AndroidRuntime(  957):        at android.content.res.AssetManager.getAssetRemainingLength(Native Method)
					 * E/AndroidRuntime(  957):        at android.content.res.AssetManager.access$300(AssetManager.java:36)
					 * E/AndroidRuntime(  957):        at android.content.res.AssetManager$AssetInputStream.available(AssetManager.java:523)
					 * E/AndroidRuntime(  957):        at java.io.InputStreamReader.read(InputStreamReader.java:431)
					 * E/AndroidRuntime(  957):        at java.io.BufferedReader.fillbuf(BufferedReader.java:130)
					 * E/AndroidRuntime(  957):        at java.io.BufferedReader.readLine(BufferedReader.java:353)
					 * E/AndroidRuntime(  957):        at com.senspodapp.service.LogplayerService$ConnectedThread.run(LogplayerService.java:97)
					 */
				}
			}
		}

		public void shutdown() {
			stop = true;
			if (! hasReadAnything) return;
			if (mmInStream != null) {
				try {
					mmInStream.close();
				} 
				catch (IOException e) {
					Log.e(TAG, "close() of InputStream failed.");
				}
			}
		}

		public void cancel() {
			try {
				mmInStream.close();
			} 
			catch (IOException e) {
				Log.e(TAG, "close() of input stream failed", e);
			}
		}

	}
}