package com.senspodapp.service;

import backport.android.bluetooth.BluetoothDevice;

import com.senspodapp.service.IDeviceManagerServiceCallback;

interface IDeviceManagerService {
    /**
     * Request the PID of this service, to do evil things with it.
     */
    int getPid();
	String getConnectedDeviceName();
    
    void connectBluetoothDevice(in BluetoothDevice device);
	void disconnectBluetoothDevice(in String deviceName);
	
	void connectLogplayer(in String assetName, in int messageInterval);
	void disconnectLogplayer();
	
	void registerCallback(in IDeviceManagerServiceCallback cb);
	void unregisterCallback(in IDeviceManagerServiceCallback cb);
}