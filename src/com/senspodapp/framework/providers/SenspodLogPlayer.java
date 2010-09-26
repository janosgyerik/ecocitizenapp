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

package com.senspodapp.framework.providers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.util.Log;

import com.senspodapp.data.SensorInfo;

public class SenspodLogPlayer extends SensorDataProviderBase {
    // Debugging
    static final String TAG = "SenspodLogPlayer";
    static final boolean D = true;

    // Member fields
    private InputStream mmInStream = null;
    private ConnectedThread mConnectedThread;
    
    // todo: these should come from properties file
    private final int messageInterval = 1000;
    private final String sampleFileName = "CitySenspodSample2.txt";

    public SenspodLogPlayer(Context context) {
    	this.sensorInfo = new SensorInfo();
    	this.sensorInfo.deviceID = "00:00:00";
    	this.sensorInfo.deviceName = "Sample";
    	
    	try {
			mmInStream = context.getAssets().open(sampleFileName);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        
        // Start the thread sending dummy data
        mConnectedThread = new ConnectedThread();
        mConnectedThread.start();
    }

    //TODO
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

			//TODO
	        //sendConnectedDeviceName("Simulator");
	        
			int count = 0;
			while (! shouldStop) {
				count++;
				if (count > 10) shouldStop = true;
				try {
	            	String line = reader.readLine();
	            	if (line != null) {
		            	receivedSentenceString(line);
	            	}
	            	try {
	            		Thread.sleep(messageInterval);
	            	}
	            	catch (InterruptedException e) {
	            		
	            	}
				} catch (IOException e) {
	                Log.e(TAG, "disconnected", e);
	                //connectionLost();//TODO
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

