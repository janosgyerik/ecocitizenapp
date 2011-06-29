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

import com.ecocitizen.common.MessageType;
import com.ecocitizen.common.bundlewrapper.SentenceBundleWrapper;

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
		return SentenceBundleWrapper.makeBundle(mSensorId, sentence, mGpsLocationListener.getLastLocationBundle());
	}
	
	/**
	 * Send message to owner's handler.
	 */
	private void sendSensorNameMsg(int messageType) {
		switch (messageType) {
		case MessageType.SM_CONNECTION_FAILED:
		case MessageType.SM_DEVICE_ADDED:
		case MessageType.SM_DEVICE_CLOSED:
		case MessageType.SM_DEVICE_LOST:
			mHandler.obtainMessage(messageType, mSensorName).sendToTarget();
			break;
		}
	}
	
	void sendSentenceLineMsg(String line) {
		Bundle bundle = getSensorDataBundle(line);
		mHandler.obtainMessage(MessageType.SENTENCE, bundle).sendToTarget();
	}

	/**
	 * Connection failed, notify owner's handler.
	 */
	void sendConnectFailedMsg() {
		sendSensorNameMsg(MessageType.SM_CONNECTION_FAILED);
	}

	/**
	 * Connection established, notify owner's handler.
	 */
	void sendConnectedMsg() {
		sendSensorNameMsg(MessageType.SM_DEVICE_ADDED);
	}

	/**
	 * Connection closed (no more data), notify owner's handler.
	 */
	void sendConnectionClosedMsg() {
		sendSensorNameMsg(MessageType.SM_DEVICE_CLOSED);
	}

	/**
	 * Connection lost, notify owner's handler.
	 */
	void sendConnectionLostMsg() {
		sendSensorNameMsg(MessageType.SM_DEVICE_LOST);
	}
}
