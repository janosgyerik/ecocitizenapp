/*
 * Copyright (C) 2010-2012 Eco Mobile Citizen
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

package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import com.ecocitizen.common.Base64;

import android.location.Location;
import android.os.Bundle;

public class SensorDataBundleWrapper extends AbstractBundleWrapper {
	
	private static final String BB_SEQUENCE_NUMBER = "10";
	private static final String BB_SENSOR_ID = "20";
	private static final String BB_SENSOR_NAME = "30";
	private static final String BB_DTZ = "40";
	private static final String BB_SENSOR_DATA = "50";
	private static final String BB_IS_BINARY = "60";
	private static final String BB_LOCATION = "70";
	
	private long sequenceNumber;
	private String sensorId;
	private String sensorName;
	private String dtz;
	private byte[] sensorData;
	private boolean binary;
	
	private LocationBundleWrapper locationBundleWrapper;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public SensorDataBundleWrapper(Bundle bundle) {
		super(bundle);
		
		sequenceNumber = bundle.getLong(BB_SEQUENCE_NUMBER);
		sensorId = bundle.getString(BB_SENSOR_ID);
		sensorName = bundle.getString(BB_SENSOR_NAME);
		dtz = bundle.getString(BB_DTZ);

		sensorData = bundle.getByteArray(BB_SENSOR_DATA);
		binary = bundle.getBoolean(BB_IS_BINARY);
		if (! binary) {
			String line = new String(sensorData);
			int indexOf_dollar = line.indexOf('$'); 
			if (indexOf_dollar > -1) {
				line = line.substring(indexOf_dollar);
				sensorData = line.getBytes();
			}
		}
		
		locationBundleWrapper = 
			new LocationBundleWrapper(getBundle().getBundle(BB_LOCATION));
	}

	public static Bundle makeBundle(long sequenceNumber, String sensorId, String sensorName, 
			byte[] data, boolean isBinary, Bundle locationBundle) {
		Bundle bundle = new Bundle();
		bundle.putLong(BB_SEQUENCE_NUMBER, sequenceNumber);
		bundle.putString(BB_SENSOR_ID, sensorId);
		bundle.putString(BB_SENSOR_NAME, sensorName);
		bundle.putString(BB_DTZ, getCurrentDTZ());
		bundle.putByteArray(BB_SENSOR_DATA, data);
		bundle.putBoolean(BB_IS_BINARY, isBinary);
		bundle.putParcelable(BB_LOCATION, locationBundle);
		
		return bundle;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public String getSensorId() {
		return sensorId;
	}
	
	public String getSensorName() {
		return sensorName;
	}
	
	public String getDtz() {
		return dtz;
	}

	public byte[] getSensorData() {
		return sensorData;
	}
	
	public boolean isBinary() {
		return binary;
	}

	public Location getLocation() {
		return locationBundleWrapper.getLocation();
	}
	
	public String toString() {
		String datarecord = new Formatter().format(
				"%s,%s,%s,%s,_",
				isBinary() ? "BASE64" : "SENTENCE",
				getSensorId(),
				getDtz(),
				isBinary() ? Base64.encodeBytes(getSensorData()) : new String(getSensorData())
		).toString();
		
		if (!locationBundleWrapper.isNull()) {
			datarecord += "," + locationBundleWrapper;
		}
		
		return datarecord;
	}
}
