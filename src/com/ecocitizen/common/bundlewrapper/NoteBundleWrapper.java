package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import android.os.Bundle;

import com.ecocitizen.common.Base64;
import com.ecocitizen.common.Util;

public class NoteBundleWrapper extends AbstractBundleWrapper {
	
	private static final String BB_LOC_START = "1";
	private static final String BB_LOC_END = "2";
	private static final String BB_DTZ = "3";
	private static final String BB_LINE = "4";
	
	private Bundle startLocationBundle;
	private Bundle endLocationBundle;
	private String dtz;
	private String note;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public NoteBundleWrapper(Bundle bundle) {
		super(bundle);
		
		startLocationBundle = bundle.getParcelable(BB_LOC_START);
		endLocationBundle = bundle.getParcelable(BB_LOC_END);
		dtz = bundle.getString(BB_DTZ);
		note = bundle.getString(BB_LINE);
	}

	/**
	 * Convenience method to build a bundle from components
	 * 
	 * @param startLocationBundle
	 * @param endLocationBundle
	 * @param note
	 * @return
	 */
	public static Bundle makeBundle(Bundle startLocationBundle, Bundle endLocationBundle, String note) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(BB_LOC_START, startLocationBundle);
		bundle.putParcelable(BB_LOC_END, endLocationBundle);
		bundle.putString(BB_DTZ, Util.getCurrentDTZ());
		bundle.putString(BB_LINE, note);
		
		return bundle;
	}

	public String getNote() {
		return note;
	}
	
	public String toString() {
		String datarecord = new Formatter().format(
				"NOTE,%s,%s,_",
				dtz,
				Base64.encodeBytes(getNote().getBytes())
				).toString();
		if (startLocationBundle != null) {
			datarecord += "," + new LocationBundleWrapper(startLocationBundle);
		}
		if (endLocationBundle != null) {
			datarecord += "," + new LocationBundleWrapper(endLocationBundle);
		}
		return datarecord;
	}

}
