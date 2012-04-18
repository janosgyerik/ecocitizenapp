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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.os.Handler;
import android.util.Log;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.ecocitizen.common.DeviceHandlerFactory;
import com.ecocitizen.common.reader.DeviceReader;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread for connecting with 
 * a device, and a thread for performing data transmissions when connected.
 */
public class BluetoothSensorManager extends SensorManager {
	// Debugging
	private static final String TAG = "BluetoothSensorManager";
	private static final boolean D = false;

	// Unique UUID for this application generated by uuidgen
	private static final UUID MY_UUID = UUID.fromString("0E8783DA-BB85-4225-948F-F0EAB948C5FF");

	private static final long IS_ALIVE_TEST_SECONDS = 30;

	// Member fields
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;

	/**
	 * The class is for one-time use only.
	 */
	private final BluetoothDevice mDevice; 

	/**
	 * Constructor. Prepares a new session.
	 * @param handler  A Handler to send messages back to the UI Activity
	 */
	public BluetoothSensorManager(Handler handler, GpsLocationListener gpsLocationListener, 
			BluetoothDevice device) {
		super(getDeviceId(device), device.getName(), handler, gpsLocationListener);

		mDevice = device;
	}

	protected static String getDeviceId(BluetoothDevice device) {
		return device.getAddress().replaceAll(":", "_");
	}

	public synchronized void connect() throws Exception {
		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		try {
			mConnectThread = new ConnectThread();
			mConnectThread.start();
		}
		catch (Exception e) {
			this.sendConnectFailedMsg();
			throw e; // propagate to caller
		}
	}

	private synchronized void connected(BluetoothSocket socket) {
		// Cancel the thread that completed the connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		try {
			// Start the thread to manage the connection and perform transmissions
			mConnectedThread = new ConnectedThread(socket);
			mConnectedThread.start();
		}
		catch (Exception e) {
			this.sendConnectFailedMsg();
		}
	}

	@Override
	public void shutdown() {
		if (D) Log.d(TAG, "stop");
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
	}

	/**
	 * This thread runs while creating a connection with a remote device.
	 * It shuts itself down after successful connection.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;

		public ConnectThread() throws Exception {
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
				Method m = mDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
				tmp = (BluetoothSocket) m.invoke(mDevice, 1);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
				throw new Exception("failed to create bluetooth socket");
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				Log.e(TAG, "mmSocket.connect() failed", e);
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() socket during connection failure", e2);
				}
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothSensorManager.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		private boolean stop;

		public ConnectedThread(BluetoothSocket socket) throws Exception {
			if (D) Log.d(TAG, "create ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} 
			catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
				throw new Exception("failed to setup input/output streams");
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");

			final DeviceReader deviceReader = DeviceHandlerFactory.getInstance().getReader(getDeviceName(), getDeviceId());
			deviceReader.setInputStream(mmInStream);
			deviceReader.setOutputStream(mmOutStream);
			deviceReader.initialize();
			boolean isBinary = deviceReader.isBinary();

			Log.i(TAG, "Sanity check if sensor is alive: " + getDeviceName());
			ExecutorService executor = Executors.newFixedThreadPool(1);
			Callable<Boolean> isAliveTest = new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return deviceReader.readNextData() != null;
				}
			};
			Future<Boolean> future = executor.submit(isAliveTest);
			boolean isAlive = false;
			try {
				isAlive = future.get(IS_ALIVE_TEST_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e2) {
			} catch (ExecutionException e2) {
			} catch (TimeoutException e2) {
				isAlive = false;
			}

			if (! isAlive) {
				Log.w(TAG, "Device appears to be DEAD: " + getDeviceName());
				BluetoothSensorManager.this.sendConnectFailedMsg();
				closeFileHandles();
				return;
			}

			BluetoothSensorManager.this.sendConnectedMsg();

			stop = false;

			long sequenceNumber = 0;

			while (! stop) {
				try {
					byte[] data = deviceReader.readNextData();
					if (data != null) {
						sendSensorDataMsg(++sequenceNumber, data, isBinary);
					}
				} catch (Exception e) {
					Log.e(TAG, "disconnected", e);
					break;
				}
			}

			closeFileHandles();

			if (stop) {
				// clean disconnect
				BluetoothSensorManager.this.sendConnectionClosedMsg();
			}
			else {
				// interrupt, abrupt disconnect, connection lost
				BluetoothSensorManager.this.sendConnectionLostMsg();
			}
		}

		private void closeFileHandles() {
			try {
				mmInStream.close();
			} 
			catch (IOException e) {
				Log.e(TAG, "close() of InputStream failed.");
			}

			try {
				mmOutStream.close();
			} 
			catch (IOException e) {
				Log.e(TAG, "close() of OutputStream failed.");
			}

			try {
				mmSocket.close();
			} 
			catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

		public void cancel() {
			stop = true;
		}
	}

}
