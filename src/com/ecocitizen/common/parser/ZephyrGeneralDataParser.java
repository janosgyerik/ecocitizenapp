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

package com.ecocitizen.common.parser;

import java.util.LinkedList;
import java.util.List;

public class ZephyrGeneralDataParser implements SensorDataParser {
	
	static final SensorDataFilter filter = new SensorDataFilter(
			SensorDataType.HeartRate,
			SensorDataType.RespirationRate,
			SensorDataType.SkinTemperature,
			//SensorDataType.BloodPressure,
			SensorDataType.Posture,
			SensorDataType.Activity
			);
	
	static final int HEART_RATE_POS = 12;
	static final int RESPIRATION_RATE_POS = 14;
	static final int SKIN_TEMPERATURE_POS = 16;
	static final int POSTURE_POS = 18;
	static final int ACTIVITY_POS = 20;
	static final int BLOOD_PRESSURE_POS = 50;

	public List<SensorData> getSensorData(byte[] bytes, SensorDataFilter filter) {
		List<SensorData> sensorDataList = new LinkedList<SensorData>();
		
		for (SensorDataType dataType : filter.dataTypes) {
			SensorData data = null;
			String value;
			
			switch (dataType) {
			case HeartRate:
				value = getValue(bytes, HEART_RATE_POS, 1);
				data = new SensorData(SensorDataType.HeartRate, "BPM", value);
				break;
			case RespirationRate:
				value = getValue(bytes, RESPIRATION_RATE_POS, 2, .1f);
				data = new SensorData(SensorDataType.RespirationRate, "BPM", value);
				break;
			case SkinTemperature:
				value = getValue(bytes, SKIN_TEMPERATURE_POS, 2, .1f);
				data = new SensorData(SensorDataType.SkinTemperature, "ºC", value);
				break;
			case Posture:
				value = getSignedValue(bytes, POSTURE_POS, 2);
				data = new SensorData(SensorDataType.Posture, "deg", value);
				break;
			case Activity:
				value = getValue(bytes, ACTIVITY_POS, 2, .01f);
				data = new SensorData(SensorDataType.Activity, "VMU/s", value);
				break;
			case BloodPressure:
				value = getValue(bytes, BLOOD_PRESSURE_POS, 2, 0.001f);
				data = new SensorData(SensorDataType.BloodPressure, "Hg", value);
				break;
			}
			
			if (data != null) {
				sensorDataList.add(data);
			}
		}
		
		return sensorDataList;
	}
	
	private int getIntValue(byte[] buffer, int pos, int bytes) {
		int value;
		if (bytes == 1) {
			value = buffer[pos] & 0xff;
		}
		else if (bytes == 2) {
			value = (buffer[pos] & 0xff) + 256 * (buffer[pos+1] & 0xff);
		}
		else {
			value = 0;
		}
		return value;
	}
	
	private String getValue(byte[] buffer, int pos, int bytes) {
		return "" + getIntValue(buffer, pos, bytes);
	}
	
	private String getValue(byte[] buffer, int pos, int bytes, float resolution) {
		return String.format("%.1f", getIntValue(buffer, pos, bytes) * resolution);
	}
	
	private String getSignedValue(byte[] buffer, int pos, int bytes) {
		int value = getIntValue(buffer, pos, bytes);
		if (bytes == 1) {
			if (value > 127) value -= 256;
		}
		else if (bytes == 2) {
			if (value > 256*256/2-1) value -= 256*256;
		}
		return "" + value;
	}

	public List<SensorData> getSensorData(byte[] bytes) {
		return getSensorData(bytes, filter);
	}
	
}
