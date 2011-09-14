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

public class SensorData {

	SensorDataType dataType;
	String name;
	String unit;
	float floatValue;
	String strValue;
	int level;
	
	public SensorData(SensorDataType dataType,
			String name, String unit, String strValue) {
		this.dataType = dataType;
		this.name = name;
		this.unit = unit;
		this.strValue = strValue;
		
		float floatValue;
		try {
			floatValue = Float.parseFloat(strValue);
		} 
		catch (NumberFormatException e) {
			floatValue = Float.NaN;
		}
		this.floatValue = floatValue;
		
		this.level = 0; // TODO
	}
	
	public String getUnit() {
		return unit;
	}

	public String getName() {
		return name;
	}
	
	public float getFloatValue() {
		return floatValue;
	}

	public String getStrValue() {
		return strValue;
	}

	public int getLevel() {
		return level;
	}

	public SensorDataType getDataType() {
		return dataType;
	}

}
