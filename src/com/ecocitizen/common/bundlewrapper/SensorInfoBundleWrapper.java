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

import android.os.Bundle;

public class SensorInfoBundleWrapper extends AbstractBundleWrapper {
	
	private final static String BB_SENSOR_NAME = "1";
	private final static String BB_SENSOR_ID = "2";
	
	private String sensorName;
	private String sensorId;
	
	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public SensorInfoBundleWrapper(Bundle bundle) {
		super(bundle);

		sensorName = bundle.getString(BB_SENSOR_NAME);
		sensorId = bundle.getString(BB_SENSOR_ID);
	}

	public static Bundle makeBundle(String sensorName, String sensorId) {
		Bundle bundle = new Bundle();
		
		bundle.putString(BB_SENSOR_NAME, sensorName);
		bundle.putString(BB_SENSOR_ID, sensorId);

		return bundle;
	}

	public String getSensorName() {
		return sensorName;
	}
	
	public String getSensorId() {
		return sensorId;
	}
	
}
