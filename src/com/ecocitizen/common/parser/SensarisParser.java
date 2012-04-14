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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SensarisParser implements SensorDataParser {

	public static String join(Collection<?> s, String delimiter) {
	    StringBuffer buffer = new StringBuffer();
	    Iterator<?> iter = s.iterator();
	    while (iter.hasNext()) {
	        buffer.append(iter.next());
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}
	
	public List<SensorData> getSensorData(byte[] bytes, SensorDataFilter filter) {
		String line = new String(bytes);
		List<SensorData> sensorDataList = new LinkedList<SensorData>();
		
		Set<String> psenTypes = new HashSet<String>(); 
		for (SensorDataType dataType : filter.dataTypes) {
			switch (dataType) {
			case CO2:
				psenTypes.add("CO2");
				break;
			case COx:
				psenTypes.add("COx");
				break;
			case NOx:
				psenTypes.add("NOx");
				break;
			case Noise:
				psenTypes.add("Noise");
				break;
			case Humidity:
			case Temperature:
				psenTypes.add("Hum");
				break;
			}
		}
		
		String pattern;
		if (psenTypes.isEmpty()) {
			pattern = ".*\\$PSEN,.*";
		}
		else {
			pattern = ".*\\$PSEN,(" + join(psenTypes, "|") + "),.*";
		}
		
		if (! line.matches(pattern)) return sensorDataList;
		
		int dataStartIndex = line.indexOf("$PSEN,");
		if (dataStartIndex > -1) {
			String[] cols = line.substring(dataStartIndex).split(",");
			if (cols.length < 4) return sensorDataList;
			
			SensorDataType dataType;
			String name = cols[1];
			String unit = cols[2];
			String strValue = cols[3];
			
			if (name.equals("CO2")) {
				dataType = SensorDataType.CO2;
				unit = "ppm";
				sensorDataList.add(new SensorData(dataType, unit, strValue));
			}
			else if (name.equals("COx")) {
				dataType = SensorDataType.COx;
				unit = "ppm";
				sensorDataList.add(new SensorData(dataType, unit, strValue));
			}
			else if (name.equals("NOx")) {
				dataType = SensorDataType.NOx;
				unit = "ppm";
				sensorDataList.add(new SensorData(dataType, unit, strValue));
			}
			else if (name.equals("Noise")) {
				dataType = SensorDataType.Noise;
				unit = "dB";
				sensorDataList.add(new SensorData(dataType, unit, strValue));
			}
			else if (name.equals("Hum")) {
				dataType = SensorDataType.Humidity;
				unit = "%";
				sensorDataList.add(new SensorData(dataType, unit, strValue));
				sensorDataList.add(new SensorData(SensorDataType.Temperature, "ºC", cols[5]));
			}
			else {
				dataType = SensorDataType.UNKNOWN;
				sensorDataList.add(new SensorData(dataType, unit, strValue));
			}
		}
		
		return sensorDataList;
	}
	
	public List<SensorData> getSensorData(byte[] bytes) {
		return getSensorData(bytes, new SensorDataFilter());
	}
		
}
