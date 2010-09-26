package com.senspodapp.framework.providers;

import java.util.Date;
import java.util.LinkedList;

import android.util.Log;

import com.senspodapp.data.GpsInfo;
import com.senspodapp.data.SensorInfo;
import com.senspodapp.framework.GpsProvider;
import com.senspodapp.framework.SensorDataBundle;
import com.senspodapp.framework.consumers.SensorDataConsumer;
import com.titan2x.android.senspod.Util;

public abstract class SensorDataProviderBase implements SensorDataProvider {
	LinkedList<SensorDataConsumer> consumers = new LinkedList<SensorDataConsumer>();
	
	SensorInfo sensorInfo = null;

	public void addSensorDataConsumer(SensorDataConsumer consumer) {
		consumers.add(consumer);
	}
	
	GpsProvider gpsProvider = null;
	
	public void setGpsProvider(GpsProvider gpsProvider) {
		this.gpsProvider = gpsProvider;
	}
	
	private void dispatchSensorDataBundle(SensorDataBundle bundle) {
		for (SensorDataConsumer consumer : consumers) {
			consumer.consumeSensorDataBundle(bundle);
		}
	}
	
	protected void receivedSentenceString(String str) {
		Log.d(getClass().getSimpleName(), "!! SENTENCE = " + str);
		Date timestamp = Util.getCurrentTimestamp();
		GpsInfo gps = gpsProvider.getGpsInfo();
		SensorDataBundle bundle = new SensorDataBundle(sensorInfo, timestamp, gps, str);
		dispatchSensorDataBundle(bundle);
	}
	
}
