package com.ecocitizen.common;

import android.location.Location;
import android.os.Bundle;

public class SentenceBundle {
	
	private Bundle bundle;
	private String sentenceLine;
	private boolean locationIsNull = false;
	private Location location;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public SentenceBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	public String getSentenceLine() {
		if (sentenceLine == null) {
			String line = bundle.getString(BundleKeys.SENTENCE_LINE);
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
			Bundle locationBundle = bundle.getBundle(BundleKeys.LOCATION_BUNDLE);
			if (locationBundle == null) {
				locationIsNull = true;
				return null;
			}
			location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
		}
		return location;
	}
}
