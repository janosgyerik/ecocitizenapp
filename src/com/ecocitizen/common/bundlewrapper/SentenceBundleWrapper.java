package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import android.location.Location;
import android.os.Bundle;

public class SentenceBundleWrapper extends AbstractBundleWrapper {
	
	private final static String BB_SEQUENCE_NUMBER = "10";
	private final static String BB_SENSOR_ID = "20";
	private final static String BB_SENSOR_NAME = "30";
	private final static String BB_DTZ = "40";
	private final static String BB_SENTENCE = "50";
	private final static String BB_LOCATION = "60";
	
	private long sequenceNumber;
	private String sensorId;
	private String sensorName;
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
		
		sequenceNumber = bundle.getLong(BB_SEQUENCE_NUMBER);
		sensorId = bundle.getString(BB_SENSOR_ID);
		sensorName = bundle.getString(BB_SENSOR_NAME);
		dtz = bundle.getString(BB_DTZ);
		
		String line = bundle.getString(BB_SENTENCE);
		int indexOf_dollar = line.indexOf('$'); 
		if (indexOf_dollar > -1) {
			line = line.substring(indexOf_dollar);
		}
		sentenceLine = line;
		
		locationBundleWrapper = 
			new LocationBundleWrapper(getBundle().getBundle(BB_LOCATION));
	}

	public static Bundle makeBundle(long sequenceNumber, String sensorId, String sensorName, 
			String sentence, Bundle locationBundle) {
		Bundle bundle = new Bundle();
		bundle.putLong(BB_SEQUENCE_NUMBER, sequenceNumber);
		bundle.putString(BB_SENSOR_ID, sensorId);
		bundle.putString(BB_SENSOR_NAME, sensorName);
		bundle.putString(BB_DTZ, getCurrentDTZ());
		bundle.putString(BB_SENTENCE, sentence);
		bundle.putParcelable(BB_LOCATION, locationBundle);
		
		return bundle;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public String getSensorId() {
		return sensorId;
	}
	
	public String getSensorName() {
		return sensorName;
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
				getSensorId(),
				getDtz(),
				getSentenceLine()
		).toString();
		
		if (!locationBundleWrapper.isNull()) {
			datarecord += "," + locationBundleWrapper;
		}
		
		return datarecord;
	}
}
