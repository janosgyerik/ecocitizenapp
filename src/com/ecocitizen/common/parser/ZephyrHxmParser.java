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

public class ZephyrHxmParser implements SensorDataParser {
	
	static final SensorDataFilter filter = new SensorDataFilter(
			SensorDataType.HeartRate,
			SensorDataType.Distance,
			SensorDataType.InstSpeed,
			SensorDataType.Strides
			);
	
	static final int HEARTRATE_POS = 12;
	static final int DISTANCE_POS = 50;
	static final int INST_SPEED_POS = 52;
	static final int STRIDES_POS = 54;

	public List<SensorData> getSensorData(byte[] bytes, SensorDataFilter filter) {
		List<SensorData> sensorDataList = new LinkedList<SensorData>();
		
		/*
		for (SensorDataType dataType : filter.dataTypes) {
			SensorData data = null;
			
			switch (dataType) {
			case HeartRate:
				int heartRate = bytes.charAt(HEARTRATE_POS);
				if (heartRate >= 240) {
					heartRate = 0;
				}
				data = new SensorData(SensorDataType.HeartRate, "", Integer.toString(heartRate));
				break;
			case Distance:
				int distance = bytes.charAt(DISTANCE_POS) + 256 * bytes.charAt(DISTANCE_POS + 1);
				data = new SensorData(SensorDataType.Distance, "", Integer.toString(distance));
				break;
			case InstSpeed:
				int instSpeed = bytes.charAt(INST_SPEED_POS) + 256 * bytes.charAt(INST_SPEED_POS + 1);
				data = new SensorData(SensorDataType.InstSpeed, "", Integer.toString(instSpeed));
				break;
			case Strides:
				int strides = bytes.charAt(STRIDES_POS);
				data = new SensorData(SensorDataType.Strides, "", Integer.toString(strides));
				break;
			}
			
			if (data != null) {
				sensorDataList.add(data);
			}
		}
		*/
		
		return sensorDataList;
	}
	
	public List<SensorData> getSensorData(byte[] bytes) {
		return getSensorData(bytes, filter);
	}
		
}
