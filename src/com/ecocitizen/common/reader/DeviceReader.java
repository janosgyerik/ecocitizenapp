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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	byte[] readNextData() throws IOException;

	/**
	 * Set the input stream to read data from. 
	 * 
	 * @param inStream
	 */
	void setInputStream(InputStream inStream);

	/**
	 * Set the output stream to write data to.
	 * For many sensor types this is optional and ok to do nothing.
	 * 
	 * @param outStream
	 */
	void setOutputStream(OutputStream outStream);
	
	/**
	 * Initialize the driver, this can be sending control messages
	 * to enable broadcast messages on the device, or just a NOP.
	 * 
	 * Implementations can assume that this is called after
	 * input and output streams are set.
	 */
	void initialize();
	
	/**
	 * Return true if the data messages are in binary format,
	 * for example the Zephyr sensors.
	 * 
	 * @return
	 */
	boolean isBinary();

}
