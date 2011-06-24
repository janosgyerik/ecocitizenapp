package com.ecocitizen.service;

import java.util.Formatter;

import android.location.Location;
import android.os.Bundle;

public class BundleTools {
	
	public static String convertToDataRecord(Bundle bundle, boolean ignoreGpsSentences) {
		String line = bundle.getString(BundleKeys.SENTENCE_LINE);
		int indexOf_dollar = line.indexOf('$'); 
		if (indexOf_dollar > -1) {
			line = line.substring(indexOf_dollar);
		}
		
		/* TODO
		 * Do not upload GPS sentences.
		 * This is not a very good thing to do (not clean).
		 * But, the thing is, GPS sentences are kind of useless,
		 * because GPS information is attached anyway using Android's
		 * own GPS, which in our experience so far is better than
		 * the GPS of sensors. So, these sentences are useless,
		 * and just take up unnecessary bandwidth.
		 * In the long term however, this kind of hard coding
		 * should be controllable by advanced settings screen or something.
		 */
		if (ignoreGpsSentences && line.startsWith("$GP")) {
			return null;
		}
		
		Formatter formatter = new Formatter();
		String datarecord;
		Bundle locationBundle = bundle.getBundle(BundleKeys.LOCATION_BUNDLE);
		if (locationBundle == null) {
			String format = "SENTENCE,%s,%s,%s,_";
			datarecord = formatter.format(
					format,
					bundle.getString(BundleKeys.SENTENCE_SENSOR_ID),
					bundle.getString(BundleKeys.SENTENCE_DTZ),
					line
			).toString();
		}
		else {
			String format = "GPS,%s,%d,%f,%f,AndroidGps,%f,%f,%f,%f,_,SENTENCE,%s,%s,%s,_";
			Location location = (Location)locationBundle.getParcelable(BundleKeys.LOCATION_LOC);
			datarecord = formatter.format(
					format,
					locationBundle.getString(BundleKeys.LOCATION_DTZ),
					locationBundle.getInt(BundleKeys.LOCATION_LATLON_ID),
					location.getLatitude(),
					location.getLongitude(),
					location.getAccuracy(),
					location.getAltitude(),
					location.getBearing(),
					location.getSpeed(),
					bundle.getString(BundleKeys.SENTENCE_SENSOR_ID),
					bundle.getString(BundleKeys.SENTENCE_DTZ),
					line
			).toString();
		}
		
		return datarecord;
	}
}
