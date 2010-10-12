/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.titan2x.android.senspod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Handler;
import android.util.Log;
import backport.android.bluetooth.BluetoothDevice;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class SampleSenspodService extends BluetoothSensorService {
    // Debugging
    static final String TAG = "SimulatorSenspodService";
    static final boolean D = true;

    // Member fields
    private InputStream mmInStream = null;
    private ConnectedThread mConnectedThread;
    
    private final String mSensorId;
    private final int mMessageInterval;

    /**
     * Constructor. Prepares a new session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public SampleSenspodService(Handler handler, String sensorId, InputStream instream, int messageInterval) {
    	mHandler = handler;
    	mSensorId = sensorId;
    	mMessageInterval = messageInterval;
    	mmInStream = instream;
        
        setState(STATE_CONNECTING);
        
        // Start the thread sending dummy data
        mConnectedThread = new ConnectedThread();
        mConnectedThread.start();
    }

	@Override
	public void connect(BluetoothDevice device) {
		// this method should never be called...
	}

	@Override
	public void stopAllThreads() {
    	if (mConnectedThread != null) mConnectedThread.shutdown();
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
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
	        
	        sendConnectedDeviceName("Simulator");
	        
			while (! stop) {
				try {
	            	String line = reader.readLine();
	            	if (line != null) {
	            		hasReadAnything = true;
	            		byte[] buffer = line.getBytes();
	            		mHandler.obtainMessage(MessageProtocol.MESSAGE_READ, buffer.length, -1, buffer).sendToTarget();
	            		try {
	            			Thread.sleep(mMessageInterval);
	            		}
	            		catch (InterruptedException e) {
	            		}
	            	}
				} catch (IOException e) {
	                Log.e(TAG, "disconnected", e);
	                connectionLost();
	                break;			
				}
			}
        }

        public void shutdown() {
        	stop = true;
        	if (! hasReadAnything) return;
        	if (mmInStream != null) {
        		try {
        			mmInStream.close();
        		} catch (IOException e) {
        			Log.e(TAG, "close() of InputStream failed.");
        		}
        	}
        }

        public void cancel() {
        	try {
        		mmInStream.close();
        	} catch (IOException e) {
        		Log.e(TAG, "close() of input stream failed", e);
        	}
        }
    }
}

