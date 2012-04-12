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
			SensorDataType.BloodPressure,
			SensorDataType.Posture,
			SensorDataType.Activity
			);
	
	static final int HEART_RATE_POS = 12;
	static final int RESPIRATION_RATE_POS = 14;
	static final int SKIN_TEMPERATURE_POS = 16;
	static final int POSTURE_POS = 18;
	static final int ACTIVITY_POS = 20;
	static final int BLOOD_PRESSURE_POS = 50;

	public List<SensorData> getSensorData(String buffer, SensorDataFilter filter) {
		byte[] bytes = buffer.getBytes();
		List<SensorData> sensorDataList = new LinkedList<SensorData>();
		
		for (SensorDataType dataType : filter.dataTypes) {
			SensorData data = null;
			int pos = 0;
			
			switch (dataType) {
			case HeartRate:
				pos = HEART_RATE_POS;
				data = new SensorData(SensorDataType.HeartRate, "BPM", Integer.toString(bytes[pos]) + " " + Integer.toString(bytes[pos+1]));
				/*
				int heartRate = getTwoByteData(bytes, HEART_RATE_POS);
				data = new SensorData(SensorDataType.HeartRate, "BPM", Integer.toString(heartRate));
				*/
				break;
			case RespirationRate:
				pos = RESPIRATION_RATE_POS;
				data = new SensorData(SensorDataType.RespirationRate, "BPM", Integer.toString(bytes[pos]) + " " + Integer.toString(bytes[pos+1]));
				/*
				int respirationRate = getTwoByteData(bytes, RESPIRATION_RATE_POS);
				data = new SensorData(SensorDataType.RespirationRate, "BPM", Integer.toString(respirationRate));
				*/
				break;
			case SkinTemperature:
				pos = SKIN_TEMPERATURE_POS;
				data = new SensorData(SensorDataType.SkinTemperature, "ºC", Integer.toString(bytes[pos]) + " " + Integer.toString(bytes[pos+1]));
				/*
				int skinTemperature = getTwoByteData(bytes, SKIN_TEMPERATURE_POS);
				data = new SensorData(SensorDataType.SkinTemperature, "ºC", Integer.toString(skinTemperature));
				*/
				break;
			case Posture:
				pos = POSTURE_POS;
				data = new SensorData(SensorDataType.Posture, "deg", Integer.toString(bytes[pos]) + " " + Integer.toString(bytes[pos+1]));
				/*
				int posture = getTwoByteData(bytes, POSTURE_POS);
				data = new SensorData(SensorDataType.Posture, "deg", Integer.toString(posture));
				*/
				break;
			case Activity:
				pos = ACTIVITY_POS;
				data = new SensorData(SensorDataType.Activity, "VMU/s", Integer.toString(bytes[pos]) + " " + Integer.toString(bytes[pos+1]));
				/*
				int activity = getTwoByteData(bytes, ACTIVITY_POS);
				data = new SensorData(SensorDataType.Activity, "VMU/s", Integer.toString(activity));
				*/
				break;
			case BloodPressure:
				pos = BLOOD_PRESSURE_POS;
				data = new SensorData(SensorDataType.BloodPressure, "Hg", Integer.toString(bytes[pos]) + " " + Integer.toString(bytes[pos+1]));
				/*
				int bloodPressure = getTwoByteData(bytes, BLOOD_PRESSURE_POS);
				data = new SensorData(SensorDataType.BloodPressure, "Hg", Integer.toString(bloodPressure));
				*/
				break;
			}
			
			if (data != null) {
				sensorDataList.add(data);
			}
		}
		
		return sensorDataList;
	}
	
	public List<SensorData> getSensorData(String bytes) {
		return getSensorData(bytes, filter);
	}
	
	private int getTwoByteData(byte[] bytes, int pos) {
		return bytes[pos] + 256 * bytes[pos];
	}
		
}
