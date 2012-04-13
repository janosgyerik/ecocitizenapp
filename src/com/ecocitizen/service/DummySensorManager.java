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

import android.os.Handler;
import android.util.Log;

public class DummySensorManager extends SensorManager {
	// Debugging
	private static final String TAG = "DummyService";
	private static final boolean D = false;
	
	public static final String SENSOR_ID = "DUMMY";
	public static final String SENSOR_NAME = "DUMMY";
	public static final byte[] SENSOR_LINE = "$PSEN,DUMMY,N.A.,0".getBytes();

	// Member fields
	private ConnectedThread mConnectedThread;

	private final int mMessageInterval;

	/**
	 * Constructor. Prepares a new session.
	 * @param handler  A Handler to send messages back to the UI Activity
	 */
	public DummySensorManager(Handler handler, GpsLocationListener gpsLocationListener, int messageInterval) {
		super(SENSOR_ID, SENSOR_NAME, handler, gpsLocationListener);
		mMessageInterval = messageInterval;
	}

	public boolean connect() {
		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread();
		mConnectedThread.start();

		sendConnectedMsg();
		
		return true;
	}

	/**
	 * Stop all threads
	 */
	@Override
	public void shutdown() {
		if (D) Log.d(TAG, "stop");
		if (mConnectedThread != null) mConnectedThread.shutdown();
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		sendConnectionClosedMsg();
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming transmissions.
	 */
	private class ConnectedThread extends Thread {
		private boolean stop = false;

		public ConnectedThread() {
			if (D) Log.d(TAG, "create ConnectedThread");
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");

			long sequenceNumber = 0;

			while (! stop) {
				sendSensorDataMsg(++sequenceNumber, SENSOR_LINE);
				try {
					Thread.sleep(mMessageInterval);
				}
				catch (InterruptedException e) {
				}
			}
		}

		public void shutdown() {
			stop = true;
		}
		
		public void cancel() {
		}
	}

}
