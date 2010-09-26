package com.senspodapp.framework;

import java.util.LinkedList;
import java.util.List;

import com.senspodapp.framework.consumers.SensorDataConsumer;
import com.senspodapp.framework.providers.SensorDataProvider;

public class DataManagerImpl implements DataManager {
	List<SensorDataConsumer> consumers = new LinkedList<SensorDataConsumer>();
	List<SensorDataProvider> providers = new LinkedList<SensorDataProvider>();
	
	GpsProvider gpsProvider = null;

	public void addSensorDataConsumer(SensorDataConsumer consumer) {
		consumers.add(consumer);

		for (SensorDataProvider provider : providers) {
			provider.addSensorDataConsumer(consumer);
		}
	}

	public void addSensorDataProvider(SensorDataProvider provider) {
		provider.setGpsProvider(gpsProvider);
		providers.add(provider);
		
		for (SensorDataConsumer consumer : consumers) {
			provider.addSensorDataConsumer(consumer);
		}
	}

	public void setGpsProvider(GpsProvider gpsProvider) {
		this.gpsProvider = gpsProvider;
		
		for (SensorDataProvider provider : providers) {
			provider.setGpsProvider(gpsProvider);
		}
	}

}
