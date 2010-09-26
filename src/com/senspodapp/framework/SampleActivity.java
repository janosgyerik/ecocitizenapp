package com.senspodapp.framework;

import com.senspodapp.framework.consumers.LogView;
import com.senspodapp.framework.consumers.SensorDataConsumer;
import com.senspodapp.framework.providers.SenspodLogPlayer;
import com.senspodapp.framework.providers.SensorDataProvider;

import android.content.Context;

public class SampleActivity {
	public SampleActivity(
			//SensorDataConsumer treeview,
//			SensorDataConsumer debugview,
//			SensorDataConsumer compositeview,
//			GpsProvider gps,
//			SensorDataProvider pp,
			Context context
			) {
		DataManager manager = new DataManagerImpl();
		
		SensorDataConsumer consumer = new LogView();
		manager.addSensorDataConsumer(consumer);
		
		SensorDataProvider provider = new SenspodLogPlayer(context);
//		provider.setGpsProvider(gps);
		manager.addSensorDataProvider(provider);
	}
}
