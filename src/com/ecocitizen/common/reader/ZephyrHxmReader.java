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

import com.ecocitizen.common.DebugFlagManager;

public class ZephyrHxmReader implements DeviceReader {
	// Debugging
	private static final String TAG = "ZephyrReader";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(ZephyrHxmReader.class);

	private InputStream reader;

	private final int STX = 0x02;
	private final int MSGID = 0x26;
	private final int DLC = 55;
	private final int ETX = 0x03;

	@Override
	public String readNextData() throws IOException {

		/* TODO ZephyrGeneralDataParser should be generalized
		 * and share this logic.
		char[] buffer = new char[1024];
		int b = 0;
		int i = 0;

		while (true) {
			i = 0;

			// skip until the message start marker
			while ((b = reader.read()) != STX) ;

			buffer[i++] = (char) b; // STX

			// the next byte must be the message ID 
			if ((b = reader.read()) != MSGID) {
				continue;
			}

			buffer[i++] = (char) b; // MSGID

			// the next byte must be the data length, 
			// but for our current sensor it's always the same
			if ((b = reader.read()) != DLC) {
				continue;
			}

			buffer[i++] = (char) b; // DLC

			// read payload in bulk
			int cnt;
			if ((cnt = reader.read(buffer, i, b)) < 0) {
				break;
			}
			i += cnt;

			// read until the end of text marker
			while ((b = reader.read()) != ETX) {
				buffer[i++] = (char) b;                                                         
			}

			buffer[i++] = (char) b;
			
			break;
		}
		
		if (D) Log.d(TAG, "readNextData - read " + i + " bytes");
		
		return new String(buffer);
		*/
		return null;
	}

	@Override
	public void setInputStream(InputStream inStream) {
		this.reader = inStream;
	}

	@Override
	public void setOutputStream(OutputStream outStream) {
	}
}
