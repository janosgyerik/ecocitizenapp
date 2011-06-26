package com.ecocitizen.common;

import android.os.Bundle;

public abstract class AbstractBundleWrapper {
	private final Bundle bundle;
	
	public AbstractBundleWrapper(Bundle bundle) {
		this.bundle = bundle;
	}
	
	public Bundle getBundle() {
		return bundle;
	}
}
