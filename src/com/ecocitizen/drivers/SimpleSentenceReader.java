package com.ecocitizen.drivers;

import java.io.BufferedReader;
import java.io.IOException;

public class SimpleSentenceReader implements DeviceReader {
	
	private final BufferedReader reader;

	public SimpleSentenceReader(BufferedReader reader) {
		this.reader = reader;
	}

	public String readNextData() throws IOException {
		return reader.readLine();
	}

}
