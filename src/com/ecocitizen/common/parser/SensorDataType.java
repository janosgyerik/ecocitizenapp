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

import java.util.HashMap;
import java.util.Map;

public enum SensorDataType {
	UNKNOWN,
	
	// Sensaris
	CO2, 
	COx,
	NOx,
	Noise,
	Humidity,
	Temperature,
	
	// Zephyr
	HeartRate, 
	Distance,
	InstSpeed,
	Strides,
	
	// Zephyr BioHarness
	RespirationRate,
	SkinTemperature,
	BloodPressure, 
	Posture,
	Activity,
	;

	static Map<Integer, String> names;
	static {
		names = new HashMap<Integer, String>();
		names.put(Temperature.ordinal(), "T");
		names.put(Humidity.ordinal(), "RH");
		names.put(HeartRate.ordinal(), "HR");
		names.put(RespirationRate.ordinal(), "RR");
		names.put(SkinTemperature.ordinal(), "SkinT");
	}
	public String getShortName() {
		if (names.containsKey(ordinal())) {
			return names.get(ordinal());
		}
		return name();
	}

}
