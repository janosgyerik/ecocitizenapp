package com.senspodapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Handler;
import android.util.Log;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;

public class LogplayerService extends BluetoothSensorService {
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
    public LogplayerService(Handler handler, InputStream instream, int messageInterval) {
    	mState = STATE_NONE;
    	mHandler = handler;
    	mMessageInterval = messageInterval;
    	mmInStream = instream;
    }
 
    /**
     * @param device  Should be null.
     */
    @Override
    public synchronized void connect(BluetoothDevice device) {
        mDeviceName = "Logplayer";
        
        if (D) Log.d(TAG, "connect to: " + mDeviceName);

        setState(STATE_CONNECTING);
        
        connected(null, null);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread();
        mConnectedThread.start();

        // Send the name of the connected device back to the Device Manager
        mHandler.obtainMessage(MessageType.SENSORCONNECTION_SUCCESS, mDeviceName).sendToTarget();

        setState(STATE_CONNECTED);
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectedThread != null) mConnectedThread.shutdown();
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        
        setState(STATE_NONE);
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
	            		mHandler.obtainMessage(MessageType.SENTENCE, line).sendToTarget();
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

