package com.senspodapp.framework;

import com.senspodapp.framework.consumers.SensorDataConsumer;
import com.senspodapp.framework.providers.SensorDataProvider;

/**
 * The central piece of the architecture, connecting the components together.
 * 
 * @author janos
 *
 */
public interface DataManager {
	void setGpsProvider(GpsProvider gpsProvider);
	
	void addSensorDataConsumer(SensorDataConsumer consumer);
	
	void addSensorDataProvider(SensorDataProvider provider);
}
