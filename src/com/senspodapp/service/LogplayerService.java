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
    		InputStream instream, int messageInterval) {
    	mHandler = handler;
    	mGpsLocationListener = gpsLocationListener;
    	
    	mMessageInterval = messageInterval;
    	mmInStream = instream;
    	
    	mSensorId = "LogPlayer";
    	mDeviceName = "LogPlayer";
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
				} 
				catch (IOException e) {
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