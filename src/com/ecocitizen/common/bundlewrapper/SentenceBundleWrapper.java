package com.ecocitizen.common.bundlewrapper;

import com.ecocitizen.common.BundleKeys;

import android.location.Location;
import android.os.Bundle;

public class SentenceBundleWrapper extends AbstractBundleWrapper {
	
	private String sensorID;
	private String sentenceLine;
	
	private boolean locationIsNull = false;
	private Location location;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public SentenceBundleWrapper(Bundle bundle) {
		super(bundle);
	}

	public String getSensorID() {
		if (sensorID == null) {
			sensorID = getBundle().getString(BundleKeys.SENTENCE_SENSOR_ID);
		}
		return sensorID;
	}

	public String getSentenceLine() {
		if (sentenceLine == null) {
			String line = getBundle().getString(BundleKeys.SENTENCE_LINE);
			int indexOf_dollar = line.indexOf('$'); 
			if (indexOf_dollar > -1) {
				line = line.substring(indexOf_dollar);
			}
			sentenceLine = line;
		}
		return sentenceLine;
	}

	public Location getLocation() {
		if (!locationIsNull && location == null) {
			Bundle locationBundle = getBundle().getBundle(BundleKeys.LOCATION_BUNDLE);
			if (locationBundle == null) {
				locationIsNull = true;
				return null;
			}
			location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
		}
		return location;
	}
}