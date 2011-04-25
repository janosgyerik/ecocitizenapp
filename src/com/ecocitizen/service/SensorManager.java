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

import android.os.Bundle;
import android.os.Handler;

/**
 * Base class for handling communications with sensor devices.
 * 
 * A SensorManager can only ever connect to one device. 
 * The peer device must be specified at construct time, and
 * cannot be changed later to a different device.
 */
abstract public class SensorManager {
	/**
	 * Unique ID of the sensor, used in filters on the map website.
	 */
	private final String mSensorId;
	
	/**
	 * Simple short name, used as an alias when identifying the sensor.
	 */
	private final String mSensorName;

	/**
	 * Shared Handler received from the owner object, 
	 * for sending sentences and lifecycle event notifications.
	 */
	private final Handler mHandler;

	/** 
	 * Shared location listener received from the owner object,
	 * for attaching GPS info to sentence data.
	 */
	private final GpsLocationListener mGpsLocationListener;
	
	private static final int STATE_NONE = 0;
	private static final int STATE_CONNECTED = 1;
	private static final int STATE_CONNECTING = 2;
	private int mState = STATE_NONE;
	
	/*
	private SensorManager() {
		this.mSensorId = null;
		this.mSensorName = null;
		this.mHandler = null;
		this.mGpsLocationListener = null;
	}
	*/
	
	SensorManager(String sensorId, String sensorName, 
			Handler handler, GpsLocationListener gpsLocationListener) {
		this.mSensorId = sensorId;
		this.mSensorName = sensorName;
		this.mHandler = handler;
		this.mGpsLocationListener = gpsLocationListener;
	}
	
	/**
	 * Clean up and shut down this sensor manager.
	 */
	public abstract void shutdown();

	/**
	 * Create and return a Bundle with a sentence line
	 * and additional information such as time, location and 
	 * the ID of the originating sensor.
	 * 
	 * @param sentence
	 * @return
	 */
	private Bundle getSensorDataBundle(String sentence) {
		Bundle bundle = new Bundle();
		bundle.putBundle(BundleKeys.LOCATION_BUNDLE, mGpsLocationListener.getLastLocationBundle());
		bundle.putString(BundleKeys.SENTENCE_DTZ, Util.getCurrentDTZ());
		bundle.putString(BundleKeys.SENTENCE_SENSOR_ID, mSensorId);
		bundle.putString(BundleKeys.SENTENCE_LINE, sentence);
		
		return bundle;
	}
	
	/**
	 * Send message to owner's handler.
	 */
	private void sendToHandler(int messageType) {
		switch (messageType) {
		case MessageType.SENSORCONNECTION_SUCCESS:
			mHandler.obtainMessage(messageType, mSensorName).sendToTarget();
			break;
		case MessageType.SENSORCONNECTION_DISCONNECTSELF:
		case MessageType.SENSORCONNECTION_FAILED:
		case MessageType.SENSORCONNECTION_LOST:
		case MessageType.SENSORCONNECTION_NONE:
			mHandler.obtainMessage(messageType, mSensorName).sendToTarget();
			break;
		}
	}
	
	void sendSentenceLine(String line) {
		Bundle bundle = getSensorDataBundle(line);
		mHandler.obtainMessage(MessageType.SENTENCE, bundle).sendToTarget();
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

	/**
	 * connectionDisconnectSelf, notify owner's handler.
	 */
	void connectionDisconnectSelf() {
		sendToHandler(MessageType.SENSORCONNECTION_DISCONNECTSELF);
	}
}
