package com.ecocitizen.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ecocitizen.common.reader.DeviceReader;

public class SpecializedReader implements DeviceReader {

	@Override
	public String readNextData() throws IOException {
		return null;
	}

	@Override
	public void setInputStream(InputStream inStream) {
	}

	@Override
	public void setOutputStream(OutputStream outStream) {
	}

}
