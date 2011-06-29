package com.ecocitizen.common.bundlewrapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.os.Bundle;

public abstract class AbstractBundleWrapper {
	private final Bundle bundle;
	
	public AbstractBundleWrapper(Bundle bundle) {
		this.bundle = bundle;
	}
	
	public Bundle getBundle() {
		return bundle;
	}
	static final SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss.S");

	public static final String getCurrentDTZ() {
		return String.format("%s,%d", dtFormat.format(new Date()), 
				TimeZone.getDefault().getRawOffset() / 3600000);
	}
}
