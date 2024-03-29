/*
 * Copyright (C) 2010-2012 Eco Mobile Citizen
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

import android.os.Bundle;

oneway interface IDeviceManagerServiceCallback {
	void receivedSensorDataBundle(in Bundle bundle);
	void receivedNoteBundle(in Bundle bundle);
	
	void receivedConnectionFailed(String deviceId);
	void receivedDeviceAdded(in Bundle bundle);
	void receivedDeviceClosed(String deviceId);
	void receivedDeviceLost(String deviceId);
	void receivedAllDevicesGone();
}
