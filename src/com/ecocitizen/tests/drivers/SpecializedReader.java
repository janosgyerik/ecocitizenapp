package com.ecocitizen.tests.drivers;

import java.io.BufferedReader;
import java.io.IOException;

import com.ecocitizen.common.reader.DeviceReader;

public class SpecializedReader implements DeviceReader {

	public String readNextData() throws IOException {
		return null;
	}

	public void setBufferedReader(BufferedReader bufferedReader) {
	}

}
