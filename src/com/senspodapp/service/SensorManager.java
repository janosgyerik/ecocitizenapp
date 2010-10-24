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

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Base class for handling communications with sensor devices.
 */
abstract public class SensorManager {
    // Debugging
    private static final String TAG = "SensorManager";
    private static final boolean D = true;

	static final SimpleDateFormat dtzFormat = new SimpleDateFormat("yyyyMMddHHmmss.S,Z");
	
	String mSensorId;
    String mDeviceName;

    // handler of the owner object, for sending sentences and notifications
    Handler mHandler;
        
    void setHandler(Handler handler) {
        mHandler = handler;
    }
    
    GpsLocationListener mGpsLocationListener;
    
    void setGpsLocationListener(GpsLocationListener gpsLocationListener) {
    	mGpsLocationListener = gpsLocationListener;
    }
    
    Bundle getSensorDataBundle(String sentence) {
    	Bundle bundle = new Bundle();
    	bundle.putBundle(BundleKeys.LOCATION_BUNDLE, mGpsLocationListener.getLastLocationBundle());
    	bundle.putString(BundleKeys.SENTENCE_DTZ, dtzFormat.format(new Date()));
    	bundle.putString(BundleKeys.SENTENCE_SENSOR_ID, mSensorId);
    	bundle.putString(BundleKeys.SENTENCE_LINE, sentence);
    	return bundle;
    }
    
    /**
     * Start connection threads.
     */
    public synchronized void start() {
    	if (D) Log.d(TAG, "start");
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
    }

    /**
     * Send message to owner's handler.
     */
    void sendToHandler(int messageType) {
    	switch (messageType) {
    	case MessageType.SENSORCONNECTION_SUCCESS:
            mHandler.obtainMessage(messageType, mDeviceName).sendToTarget();
    		break;
    	case MessageType.SENSORCONNECTION_FAILED:
    	case MessageType.SENSORCONNECTION_LOST:
    	case MessageType.SENSORCONNECTION_NONE:
            mHandler.obtainMessage(messageType, mDeviceName).sendToTarget();
            mDeviceName = null;
    		break;
    	}
    }
    
    /**
     * Connection established, notify owner's handler.
     */
    void connectionSuccess() {
        sendToHandler(MessageType.SENSORCONNECTION_SUCCESS);
    }

    /**
     * Connection failed, notify owner's handler.
     */
    void connectionFailed() {
        sendToHandler(MessageType.SENSORCONNECTION_FAILED);
    }

    /**
     * Connection lost, notify owner's handler.
     */
    void connectionLost() {
        sendToHandler(MessageType.SENSORCONNECTION_LOST);
    }
    
    /**
     * Connection none, notify owner's handler.
     */
    void connectionNone() {
        sendToHandler(MessageType.SENSORCONNECTION_NONE);
    }
}
