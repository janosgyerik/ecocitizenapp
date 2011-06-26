package com.ecocitizen.common;

import java.util.Formatter;

import android.os.Bundle;

public class NoteBundle extends AbstractBundleWrapper {
	
	private String note;
	private Bundle startLocationBundle;
	private Bundle endLocationBundle;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public NoteBundle(Bundle bundle) {
		super(bundle);
	}

	/**
	 * Use this constructor when creating a new Bundle from components.
	 * 
	 * @param startLocationBundle
	 * @param endLocationBundle
	 * @param note
	 */
	public NoteBundle(Bundle startLocationBundle, Bundle endLocationBundle,
			String note) {
		this(makeBundle(startLocationBundle, endLocationBundle, note));
		this.startLocationBundle = startLocationBundle;
		this.endLocationBundle = endLocationBundle;
		this.note = note;
	}

	private static Bundle makeBundle(Bundle startLocationBundle, Bundle endLocationBundle, String note) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(BundleKeys.NOTE_LOC_START, startLocationBundle);
		bundle.putParcelable(BundleKeys.NOTE_LOC_END, endLocationBundle);
		bundle.putString(BundleKeys.NOTE_DTZ, null);
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
				Util.getCurrentDTZ(),
				getNote() // TODO base64 encode
				).toString();
		if (startLocationBundle != null) {
			datarecord += "," + startLocationBundle;
		}
		if (endLocationBundle != null) {
			datarecord += "," + endLocationBundle;
		}
		return datarecord;
	}

}
