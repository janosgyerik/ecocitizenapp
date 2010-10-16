package com.senspodapp.service;

oneway interface IDeviceManagerServiceCallback {
	void receivedSentenceData(String sensorId, String sentence);
	// todo: should be a bundle of GpsInfo, DateTime, SensorId, Sentence
	
	void receivedSensorConnectionNone();
	void receivedSensorConnectionSuccess(String deviceName);
	void receivedSensorConnectionFailed(String deviceName);
	void receivedSensorConnectionLost(String deviceName);
}
