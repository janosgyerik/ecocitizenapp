/*
 * Copyright (C) 2010-2012 Eco Mobile Citizen
 *
 * This file is part of EcoCitizen.
 *
 * EcoCitizen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EcoCitizen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EcoCitizen.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ecocitizen.common.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * This class will read data line by line,
 * suitable for sensors that send text (readable) data line by line. 
 * 
 * @author janos
 *
 */
public class SimpleSentenceReader implements DeviceReader {
	
	private BufferedReader reader;

	@Override
	public byte[] readNextData() throws IOException {
		return reader.readLine().getBytes();
	}

	@Override
	public void setInputStream(InputStream inStream) {
		this.reader = new BufferedReader(new InputStreamReader(inStream));
	}

	@Override
	public void setOutputStream(OutputStream outStream) {
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean isBinary() {
		return false;
	}

}
