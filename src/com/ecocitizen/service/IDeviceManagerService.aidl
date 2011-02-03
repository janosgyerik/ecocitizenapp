/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
 *
 * This file is part of EcoCitizen.
 *
 * EcoCitizen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EcoCitizen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EcoCitizen.  If not, see <http://www.gnu.org/licenses/>.
 */
 
package com.ecocitizen.service;

import backport.android.bluetooth.BluetoothDevice;

import com.ecocitizen.service.IDeviceManagerServiceCallback;

interface IDeviceManagerService {
    /**
     * Request the PID of this service, to do evil things with it.
     */
    int getPid();

    String getConnectedDeviceName();
    
    void connectBluetoothDevice(in BluetoothDevice device);
    void connectLogplayer(in String assetName, in int messageInterval);
    void disconnectDevice(in String deviceName);
	
    void registerCallback(in IDeviceManagerServiceCallback cb);
    void unregisterCallback(in IDeviceManagerServiceCallback cb);

    void shutdown();
}
