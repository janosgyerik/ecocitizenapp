package com.senspodapp.framework;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.senspodapp.data.AndroidGpsInfo;
import com.senspodapp.data.GpsInfo;

public class AndroidGpsProvider implements GpsProvider, LocationListener {
	private final static String TAG = "AndroidGpsProvider";

	final static String provider = "gps";
	final static int minTime = 1000;
	final static float minDistance = .1f;
	
	LocationManager locationmanager = null;
	Location lastKnownLocation = null;
	Location lastLocation = null;
	
	boolean paused = true;
	
	public AndroidGpsProvider(LocationManager locationmanager) {
		//locationmanager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		this.locationmanager = locationmanager;
		lastLocation = lastKnownLocation = locationmanager.getLastKnownLocation(provider);
	}

	public GpsInfo getGpsInfo() {
		if (lastKnownLocation == null) {
			return null;
		}
		return new AndroidGpsInfo(
				lastKnownLocation.getLatitude(),
				lastKnownLocation.getLongitude(),
				lastKnownLocation.getAccuracy(),
				lastKnownLocation.getAltitude(),
				lastKnownLocation.getSpeed()
				);
	}

	public void pause() {
		if (paused) return;
		
		locationmanager.removeUpdates(this);
	}

	public void resume() {
		if (! paused) return;
		
		locationmanager.requestLocationUpdates(provider, minTime, minDistance, this);
	}

	public void onLocationChanged(Location arg0) {
		lastLocation = arg0;
		if (lastLocation != null) lastKnownLocation = lastLocation;
	}

	public void onProviderDisabled(String arg0) {
		Log.d(TAG, "onProviderDisabled " + arg0);
	}

	public void onProviderEnabled(String arg0) {
		Log.d(TAG, "onProviderEnabled " + arg0);
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		Log.d(TAG, "onStatusChanged " + arg0);
	}
}
