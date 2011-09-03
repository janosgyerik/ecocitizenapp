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
