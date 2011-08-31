package com.ecocitizen.drivers;

import java.io.IOException;

public interface DeviceReader {

	/**
	 * Read next data from the underlying input stream, 
	 * preserving raw format as much as possible.
	 * Interpretation and processing of the data should be
	 * implemented somewhere else, close to the UI. 
	 * 
	 * @return
	 * @throws IOException 
	 */
	String readNextData() throws IOException;

}
