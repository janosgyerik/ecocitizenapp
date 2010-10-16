package com.senspodapp.service;

oneway interface IDeviceManagerServiceCallback {
	void receivedSentenceData(String sensorId, String sentence);
	// todo: add datetime and gps info
}
