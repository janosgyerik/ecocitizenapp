package com.senspodapp.service;

import backport.android.bluetooth.BluetoothDevice;

import com.senspodapp.service.IDeviceManagerServiceCallback;

interface IDeviceManagerService {
	void connectBluetoothDevice(in BluetoothDevice device);
	void disconnectBluetoothDevice(in String deviceName);
	
	void connectLogplayer(in String assetName, in int messageInterval);
	void disconnectLogplayer();
	
	void registerCallback(in IDeviceManagerServiceCallback cb);
	void unregisterCallback(in IDeviceManagerServiceCallback cb);
}