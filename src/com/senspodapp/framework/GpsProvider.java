package com.senspodapp.framework;

import com.senspodapp.data.GpsInfo;

public interface GpsProvider {
	/**
	 * Get a GpsInfo object with the current location.
	 * 
	 * @return
	 */
	GpsInfo getGpsInfo();

	/**
	 * Implementation should forgive repeated (redundant) calls.
	 */
	void pause();
	
	/**
	 * Implementation should forgive repeated (redundant) calls.
	 */
	void resume();
}
