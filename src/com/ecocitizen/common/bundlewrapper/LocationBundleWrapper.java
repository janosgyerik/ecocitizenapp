package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import com.ecocitizen.common.Util;

import android.location.Location;
import android.os.Bundle;

public class LocationBundleWrapper extends AbstractBundleWrapper {
	
	private static final String BB_LOCATION = "1";
	private static final String BB_LATLON_ID = "2";
	private static final String BB_DTZ = "3";
	
	private Location location;
	private int latlonID;
	private String dtz;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public LocationBundleWrapper(Bundle bundle) {
		super(bundle);

		if (bundle != null && !bundle.isEmpty()) {
			location = (Location)bundle.getParcelable(BB_LOCATION);
			latlonID = bundle.getInt(BB_LATLON_ID);
			dtz = bundle.getString(BB_DTZ);
		}
	}
	
	public void updateLocation(Location location, int latlonID) {
		getBundle().putParcelable(BB_LOCATION, location);
		getBundle().putInt(BB_LATLON_ID, latlonID);
		getBundle().putString(BB_DTZ, Util.getCurrentDTZ());
	}

	public Location getLocation() {
		return location;
	}
	
	public boolean isNull() {
		return location == null;
	}
		
	public String toString() {
		if (location == null) {
			return null;
		}
		else {
			return new Formatter().format(
					"GPS,%s,%d,%f,%f,AndroidGps,%f,%f,%f,%f,_",
					dtz,
					latlonID,
					location.getLatitude(),
					location.getLongitude(),
					location.getAccuracy(),
					location.getAltitude(),
					location.getBearing(),
					location.getSpeed()
			).toString();
		}
	}

}
