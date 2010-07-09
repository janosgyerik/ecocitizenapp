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

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import backport.android.bluetooth.BluetoothDevice;

import com.titan2x.envdata.sentences.CO2Sentence;
import com.titan2x.envdata.sentences.GPRMCSentence;

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
    
    private final int messageInterval = 1000;
    private final String sampleFileName = "CitySenspodSample1.txt";

    /**
     * Constructor. Prepares a new session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public SampleSenspodService(Context context, Handler handler) {
    	try {
			mmInStream = context.getAssets().open(sampleFileName);
		} catch (IOException e) {
			e.printStackTrace();
			mHandler = null;
			return;
		}
    	mHandler = handler;
        
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
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
	}

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming transmissions.
     */
    private class ConnectedThread extends Thread {
    	private boolean shouldStop = false;
    	
        public ConnectedThread() {
            Log.d(TAG, "create ConnectedThread");
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

			BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream));
	        
	        sendConnectedDeviceName("Simulator");
	        
			String prevGPS = null;

			while (! shouldStop) {
				try {
	            	String line = reader.readLine();
	            	if (line != null) {
	            		if (line.startsWith("$GP")) {
	            			prevGPS = line;
	            		}
	            		else if (line.startsWith("$PSEN,CO2")) {
	            			GPRMCSentence gprmc = new GPRMCSentence(prevGPS);
	            			CO2Sentence co2 = new CO2Sentence(line);
	            			EnvDataMessage msg = new EnvDataMessage();
	            			msg.gprmc = gprmc;
	            			msg.co2 = co2;
	            			// Send the obtained bytes to the UI Activity
	            			byte[] buffer = msg.toByteArray();
	            			mHandler.obtainMessage(MessageProtocol.MESSAGE_READ, buffer.length, -1, buffer)
	            			.sendToTarget();
		            		try {
								Thread.sleep(messageInterval);
							} catch (InterruptedException e) {
								//e.printStackTrace();
							}
	            		}
	            	}
				} catch (IOException e) {
	                Log.e(TAG, "disconnected", e);
	                connectionLost();
	                break;			
				}
			}
        }

        public void cancel() {
        	try {
        		shouldStop = true;
        		mmInStream.close();
        	} catch (IOException e) {
        		Log.e(TAG, "close() of input stream failed", e);
        	}
        }
    }
}

