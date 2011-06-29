package com.ecocitizen.common;

import java.util.Formatter;

import android.location.Location;
import android.os.Bundle;

public class LocationBundle extends AbstractBundleWrapper {
	
	private final Location location;
	private final int latlonID;
	private final String dtz;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public LocationBundle(Bundle bundle) {
		super(bundle);
		
		location = (Location)bundle.getParcelable(BundleKeys.LOCATION_LOC);
		if (location != null) {
			latlonID = bundle.getInt(BundleKeys.LOCATION_LATLON_ID);
			dtz = bundle.getString(BundleKeys.LOCATION_DTZ);
		}
		else {
			latlonID = 0;
			dtz = null;
		}
	}

	public Location getLocation() {
		return location;
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
