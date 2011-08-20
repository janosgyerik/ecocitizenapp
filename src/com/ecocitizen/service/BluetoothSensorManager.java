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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import com.ecocitizen.common.DebugFlagManager;

import android.os.Handler;
import android.util.Log;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread for connecting with 
 * a device, and a thread for performing data transmissions when connected.
 */
public class BluetoothSensorManager extends SensorManager {
	// Debugging
	private static final String TAG = "BluetoothSensorManager";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(BluetoothSensorManager.class);

	// Unique UUID for this application generated by uuidgen
	private static final UUID MY_UUID = UUID.fromString("0E8783DA-BB85-4225-948F-F0EAB948C5FF");

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
	
	private static String getDeviceId(BluetoothDevice device) {
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
	 * Write to the ConnectedThread in an unsynchronized manner
	 * @param out The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		if (mConnectedThread == null) return;
		
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
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

			BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));
			
			BluetoothSensorManager.this.sendConnectedMsg();
			
			stop = false;

			long sequenceNumber = 0;
			
			while (! stop) {
				try {
					String line = reader.readLine();
					if (line != null) {
						sendSentenceLineMsg(++sequenceNumber, line);
					}
				} catch (IOException e) {
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

		/**
		 * Write to the connected OutStream.
		 * @param buffer  The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
			} 
			catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			stop = true;
		}
	}

}
