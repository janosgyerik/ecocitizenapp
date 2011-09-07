package com.ecocitizen.common.reader;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * This reader will read nothing.
 * 
 * @author janos
 */
public class NullReader implements DeviceReader {

	public String readNextData() throws IOException {
		return null;
	}

	public void setBufferedReader(BufferedReader reader) {
	}

}
