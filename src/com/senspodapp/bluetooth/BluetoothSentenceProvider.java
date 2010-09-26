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

package com.senspodapp.bluetooth;

import android.os.Handler;
import android.util.Log;
import backport.android.bluetooth.BluetoothDevice;

import com.senspodapp.framework.providers.SensorDataProviderBase;

/**
 * The base class for setting up Bluetooth connections and reading 
 * incoming data from bluetooth sensors,
 * with several common methods, and some abstract methods to be
 * implemented depending on the sensor.
 */
public abstract class BluetoothSentenceProvider extends SensorDataProviderBase {
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Debugging
    protected static final String TAG = "BluetoothSentenceProvider";
    protected static final boolean D = true;

    // Member fields
    protected Handler mHandler;
	protected int mState;
	
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public abstract void connect(BluetoothDevice device);


    /**
     * Stop all threads
     */
    public abstract void stopAllThreads();

    
    /**
     * Stop all threads
     */
    public void stop() {
        if (D) Log.d(TAG, "stop");
        stopAllThreads();
        connectionClosed();
    }

    
    /**
     * Return the current connection state. */
	public int getState() {
		return mState;
	}

	
    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    protected synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(MessageProtocol.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }


    /**
     * Indicate that the connection was successfully closed and notify the UI Activity.
     */
    protected void connectionClosed() {
        setState(STATE_NONE);

        /*
        // Send a message back to the Activity
        Message msg = mHandler.obtainMessage(MessageProtocol.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageProtocol.TOAST, "Disconnected");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */
    }

    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    protected void connectionFailed() {
        setState(STATE_NONE);

        /*
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MessageProtocol.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageProtocol.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */
    }

    
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    protected void connectionLost() {
        setState(STATE_NONE);

        /*
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MessageProtocol.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageProtocol.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */
    }
    
    /**      
     * Send the name of the connected device back to the UI Activity,
     * and set state to STATE_CONNECTED.
     * @param deviceName
     */
    protected void sendConnectedDeviceName(String deviceName) {
    	/*
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MessageProtocol.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MessageProtocol.DEVICE_NAME, deviceName);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
        */
    }

}
