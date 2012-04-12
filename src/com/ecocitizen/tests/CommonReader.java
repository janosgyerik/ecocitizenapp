package com.ecocitizen.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import com.ecocitizen.common.reader.DeviceReader;

public class CommonReader implements DeviceReader {

	@Override
	public String readNextData() throws IOException {
		return null;
	}

	@Override
	public void setBufferedReader(BufferedReader bufferedReader) {
	}

	@Override
	public void setOutputStream(OutputStream outStream) {
	}

}
