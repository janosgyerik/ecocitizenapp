package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import android.location.Location;
import android.os.Bundle;

import com.ecocitizen.common.BundleKeys;

public class SentenceBundleWrapper extends AbstractBundleWrapper {
	
	private String sensorID;
	private String dtz;
	private String sentenceLine;
	
	private LocationBundleWrapper locationBundleWrapper;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public SentenceBundleWrapper(Bundle bundle) {
		super(bundle);
		
		sensorID = bundle.getString(BundleKeys.SENTENCE_SENSOR_ID);
		dtz = bundle.getString(BundleKeys.SENTENCE_DTZ);
		
		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		int indexOf_dollar = line.indexOf('$'); 
		if (indexOf_dollar > -1) {
			line = line.substring(indexOf_dollar);
		}
		sentenceLine = line;
		
		locationBundleWrapper = 
			new LocationBundleWrapper(getBundle().getBundle(BundleKeys.LOCATION_BUNDLE));
	}

	public String getSensorID() {
		return sensorID;
	}
	
	public String getDtz() {
		return dtz;
	}

	public String getSentenceLine() {
		return sentenceLine;
	}

	public Location getLocation() {
		return locationBundleWrapper.getLocation();
	}
	
	public String toString() {
		String datarecord = new Formatter().format(
				"SENTENCE,%s,%s,%s,_",
				getSensorID(),
				getDtz(),
				getSentenceLine()
		).toString();
		
		if (!locationBundleWrapper.isNull()) {
			datarecord += "," + locationBundleWrapper;
		}
		
		return datarecord;
	}
}
