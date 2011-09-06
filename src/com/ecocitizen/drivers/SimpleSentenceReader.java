package com.ecocitizen.drivers;

import java.io.BufferedReader;
import java.io.IOException;

public class SimpleSentenceReader implements DeviceReader {
	
	private BufferedReader reader;

	public String readNextData() throws IOException {
		return reader.readLine();
	}

	public void setBufferedReader(BufferedReader reader) {
		this.reader = reader;
	}

}
