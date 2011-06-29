package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import com.ecocitizen.common.Base64;
import com.ecocitizen.common.BundleKeys;
import com.ecocitizen.common.Util;

import android.os.Bundle;

public class NoteBundleWrapper extends AbstractBundleWrapper {
	
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
		
		startLocationBundle = bundle.getParcelable(BundleKeys.NOTE_LOC_START);
		endLocationBundle = bundle.getParcelable(BundleKeys.NOTE_LOC_END);
		dtz = bundle.getString(BundleKeys.NOTE_DTZ);
		note = bundle.getString(BundleKeys.NOTE_LINE);
	}

	/**
	 * Use this constructor when creating a new Bundle from components.
	 * 
	 * @param startLocationBundle
	 * @param endLocationBundle
	 * @param note
	 */
	public NoteBundleWrapper(Bundle startLocationBundle, Bundle endLocationBundle,
			String note) {
		this(makeBundle(startLocationBundle, endLocationBundle, note));
		
		this.startLocationBundle = startLocationBundle;
		this.endLocationBundle = endLocationBundle;
		this.dtz = Util.getCurrentDTZ();
		this.note = note;
	}

	private static Bundle makeBundle(Bundle startLocationBundle, Bundle endLocationBundle, String note) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(BundleKeys.NOTE_LOC_START, startLocationBundle);
		bundle.putParcelable(BundleKeys.NOTE_LOC_END, endLocationBundle);
		bundle.putString(BundleKeys.NOTE_DTZ, Util.getCurrentDTZ());
		bundle.putString(BundleKeys.NOTE_LINE, note);
		
		return bundle;
	}

	public String getNote() {
		if (note == null) {
			note = getBundle().getString(BundleKeys.NOTE_LINE);
		}
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