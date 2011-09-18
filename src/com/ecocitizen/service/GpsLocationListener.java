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

import com.ecocitizen.common.DebugFlagManager;
import com.ecocitizen.common.bundlewrapper.LocationBundleWrapper;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/*
 * To test in emulator:
 * 0. The AVD must have GPS support enabled in Hardware properties.
 * 1. telnet localhost 5554
 * 2. geo fix -82.411629 28.054553
 *            ^^^ LONG   ^^^ LAT
 */
public class GpsLocationListener implements LocationListener {
	// Debugging
	private static final String TAG = "GpsLocationListener";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(GpsLocationListener.class);

	private LocationManager mLocationManager = null;
	private Location mLastLocation = null;
	private LocationBundleWrapper mLocationBundleWrapper = 
		new LocationBundleWrapper(new Bundle());

	// This is used to identify locations that have identical latlon.
	// It is incremented when a location update is received with 
	// different latlon then the previous location.
	// When attaching GPS data to sensor data, 0 is used for latlon_id
	// when there is no GPS data, and the location should be flagged invalid.
	private int latlonID = 0; 

	// TODO Perhaps these should be in SharedPreferences or props.xml
	private static final String PROVIDER = LocationManager.GPS_PROVIDER;
	private static final int    MIN_TIME = 1000;
	private static final float  MIN_DISTANCE = .1f;

	public GpsLocationListener(LocationManager locationManager) {
		mLocationManager = locationManager;
		
		// this is sometimes useful for debugging
		//
		//Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		//onLocationChanged(location);
		
		/*
		for (String provider : mLocationManager.getAllProviders()) {
			if (D) Log.d(TAG, "location provider=" + provider + ", isEnabled=" + mLocationManager.isProviderEnabled(provider));
		}
		*/
	}

	public void requestLocationUpdates() {
		if (mLocationManager == null) return;
		mLocationManager.requestLocationUpdates(PROVIDER, MIN_TIME, MIN_DISTANCE, this);
	}

	public void removeLocationUpdates() {
		if (mLocationManager == null) return;
		mLocationManager.removeUpdates(this);
	}

	public Location getLastLocation() {
		return mLastLocation;
	}

	public Bundle getLastLocationBundle() {
		return mLastLocation == null ? null : mLocationBundleWrapper.getBundle();
	}

	private void updateLastKnownLocation(Location location) {
		++latlonID;
		mLastLocation = location;
		mLocationBundleWrapper.updateLocation(location, latlonID);
	}

	public void onLocationChanged(Location location) {
		if (mLastLocation == null) {
			updateLastKnownLocation(location);
		}
		else if (location.getLatitude() == mLastLocation.getLatitude()
				&& location.getLongitude() == mLastLocation.getLongitude()) {
			// same latlon as last one
		}
		else {
			updateLastKnownLocation(location);
		}
		
		mLastLocation = location;
	}

	public void onProviderDisabled(String provider) {
		if (D) Log.d(TAG, "onProviderDisabled");
	}

	public void onProviderEnabled(String provider) {
		if (D) Log.d(TAG, "onProviderEnabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (D) Log.d(TAG, "onStatusChanged");
	}

}
