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

package com.ecocitizen.common.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PsenSentenceParser implements SensorDataParser {

	String pattern;
	double[] levelBoundaries;
	
	private SensorDataFilter filter;

	public PsenSentenceParser() {
		this("$PSEN,", new double[]{});
	}
	
	public PsenSentenceParser(String pattern) {
		this(pattern, new double[]{});
	}

	public PsenSentenceParser(String pattern, double[] levelBoundaries) {
		this.pattern = pattern;
		this.levelBoundaries = levelBoundaries;
	}

	private int getLevel(float value) {
		int level = 0;
		for (; level < levelBoundaries.length; ++level) {
			if (value < levelBoundaries[level]) break;
		}
		return level;
	}

	public static String join(Collection s, String delimiter) {
	    StringBuffer buffer = new StringBuffer();
	    Iterator iter = s.iterator();
	    while (iter.hasNext()) {
	        buffer.append(iter.next());
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}
	
	public List<SensorData> getSensorData(String bytes, SensorDataFilter filter) {
		String line = bytes;
		List<SensorData> sensorDataList = new LinkedList<SensorData>();
		SensorData data = new SensorData();
		
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

			data.name = cols[1];
			data.unit = cols[2];
			try {
				data.floatValue = Float.parseFloat(cols[3]);
				data.strValue = String.valueOf(data.floatValue);
			} catch (NumberFormatException e) {
				data.strValue = cols[3];
			}
			data.level = getLevel(data.floatValue);

			sensorDataList.add(data);
		}
		
		return sensorDataList;
	}

	public List<SensorData> getSensorData(String bytes) {
		return getSensorData(bytes, new SensorDataFilter());
	}
	
	String unit;
	String name;
	float floatValue;
	String strValue;
	int level;
	
	@Deprecated
	public String getUnit() {
		return data.unit;
	}

	@Deprecated
	public String getMetric() {
		return data.unit;
	}

	@Deprecated
	public String getName() {
		return data.name;
	}
	
	@Deprecated
	public float getFloatValue() {
		return data.floatValue;
	}

	@Deprecated
	public String getStrValue() {
		return data.strValue;
	}

	@Deprecated
	public int getLevel() {
		return data.level;
	}
	
	private SensorData data;
	
	void reset() {
		data.floatValue = Float.NaN;
		data.strValue = "";
		data.unit = "";
		data.level = 0;
	}
	
	void setLevel() {
		data.level = getLevel(data.floatValue);
	}
	
	@Deprecated
	public boolean match(String line) {
		for (SensorData data : getSensorData(line)) {
			this.data = data;
			return true;
		}
		return false;
	}

}
