package com.senspodapp.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class GpsLocationListener implements LocationListener {
	// Debugging
	private static final String TAG = "GpsLocationListener";
	private static final boolean D = true;
	
	LocationManager mLocationManager = null;
	Location mLastLocation = null;
	Location mLastKnownLocation = null;
	Bundle mLastKnownLocationBundle = new Bundle();
	
	static final SimpleDateFormat dtzFormat = new SimpleDateFormat("yyyyMMddHHmmss.S,Z");
	
	// This is used to identify locations that have identical latlon.
	// It is incremented when a location update is received with 
	// different latlon then the previous location.
	// When attaching GPS data to sensor data, 0 is used for latlon_id
	// when there is no GPS data, and the location should be flagged invalid.
	int latlon_id = 0; 
	
	// TODO: Perhaps these should be in SharedPreferences
	static final String PROVIDER = "gps";
	static final int    MIN_TIME = 1000;
	static final float  MIN_DISTANCE = .1f;
	
	void setLocationManager(LocationManager locationManager) {
		mLocationManager = locationManager;
		/*
		for (String provider : mLocationManager.getAllProviders()) {
			Log.d(TAG, "location provider=" + provider + ", isEnabled=" + mLocationManager.isProviderEnabled(provider));
		}
		*/
	}
	
	void requestLocationUpdates() {
		if (mLocationManager == null) return;
		mLocationManager.requestLocationUpdates(PROVIDER, MIN_TIME, MIN_DISTANCE, this);
	}
	
	void removeLocationUpdates() {
		if (mLocationManager == null) return;
		mLocationManager.removeUpdates(this);
	}
	
	void updateLastKnownLocation(Location location) {
		++latlon_id;
		mLastKnownLocation = location;
		mLastKnownLocationBundle.putString(BundleKeys.LOCATION_DTZ, dtzFormat.format(new Date()));
		mLastKnownLocationBundle.putInt(BundleKeys.LOCATION_LATLON_ID, latlon_id);
		mLastKnownLocationBundle.putParcelable(BundleKeys.LOCATION_LOC, mLastKnownLocation);
	}
	
	public Bundle getLastLocationBundle() {
		return mLastLocation == null ? null : mLastKnownLocationBundle;
	}
	
	public void onLocationChanged(Location location) {
		mLastLocation = location;
		
		if (location == null) return;
		
		if (mLastKnownLocation == null) {
			updateLastKnownLocation(location);
		}
		else if (location.getLatitude() == mLastKnownLocation.getLatitude()
				&& location.getLongitude() == mLastKnownLocation.getLongitude()) {
			// same latlon
		}
		else {
			updateLastKnownLocation(location);
		}
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
