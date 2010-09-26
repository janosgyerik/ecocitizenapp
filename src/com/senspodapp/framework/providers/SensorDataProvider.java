package com.senspodapp.framework.providers;

import com.senspodapp.framework.GpsProvider;
import com.senspodapp.framework.consumers.SensorDataConsumer;


/**
 * Receive sentences from some source and delegate to consumers.
 * 
 * @author janos
 *
 */
public interface SensorDataProvider {
	void setGpsProvider(GpsProvider gpsProvider);
	
	void addSensorDataConsumer(SensorDataConsumer consumer);
}
