package com.senspodapp.framework.consumers;

import com.senspodapp.framework.SensorDataBundle;

import android.util.Log;

public class LogView extends SensorDataConsumerBase {
	private final static String TAG = "LogView";
	
	@Override
	void handleSensorDataBundle(SensorDataBundle bundle) {
		Log.d(TAG, "receivedSensorDataBundle=" + bundle);
	}

}
