package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import com.ecocitizen.common.BundleKeys;

import android.location.Location;
import android.os.Bundle;

public class LocationBundleWrapper extends AbstractBundleWrapper {
	
	private final Location location;
	private final int latlonID;
	private final String dtz;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public LocationBundleWrapper(Bundle bundle) {
		super(bundle);
		
		if (bundle != null) {
			location = (Location)bundle.getParcelable(BundleKeys.LOCATION_LOC);
			latlonID = bundle.getInt(BundleKeys.LOCATION_LATLON_ID);
			dtz = bundle.getString(BundleKeys.LOCATION_DTZ);
		}
		else {
			location = null;
			latlonID = 0;
			dtz = null;
		}
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
