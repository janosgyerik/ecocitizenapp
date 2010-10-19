package com.senspodapp.service;

import android.os.Bundle;

oneway interface IDeviceManagerServiceCallback {
	void receivedSentenceBundle(in Bundle bundle);
	
	void receivedSensorConnectionNone();
	void receivedSensorConnectionSuccess(String deviceName);
	void receivedSensorConnectionFailed(String deviceName);
	void receivedSensorConnectionLost(String deviceName);
}
