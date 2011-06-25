package com.ecocitizen.common;

import java.util.Formatter;

import android.location.Location;
import android.os.Bundle;

public class NoteBundle {
	
	private Bundle bundle;
	private String note;
	private Location startLocation;
	private Location endLocation;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public NoteBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	public String getNote() {
		if (note == null) {
			note = bundle.getString(BundleKeys.NOTE_LINE);
		}
		return note;
	}
	
	public String toString() {
		return new Formatter().format(
				"NOTE,%s,%s,%s,%s,%s",
				"LOC1",
				"LOC1_DTZ", // TODO should use location bundles for these, not raw locations, 
				//it will actually help int many ways
				"LOC2",
				"LOC2_DTZ",
				getNote() // TODO base64 encode
				).toString();
	}

}
