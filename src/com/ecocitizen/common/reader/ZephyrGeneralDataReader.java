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

import android.util.Log;

import com.ecocitizen.common.DebugFlagManager;

public class ZephyrGeneralDataReader implements DeviceReader {
	// Debugging
	private static final String TAG = "ZephyrGeneralDataReader";
	private static final boolean D = DebugFlagManager.getInstance().getDebugFlag(ZephyrGeneralDataReader.class);

	private InputStream inStream;
	private OutputStream outStream;

	private final int STX = 0x02;
	private final int MSGID = 0x20;
	private final int DLC = 53;
	private final int ETX = 0x03;

	@Override
	public String readNextData() throws IOException {

		byte[] buffer = new byte[1024];
		byte b = 0;
		int i = 0;

		while (true) {
			i = 0;
			
			Log.d(TAG, "seek STX ...");
			//sendLifeSignal();
			sendEnableGeneralDataPacket();

			// skip until the message start marker
			while ((b = (byte)inStream.read()) != STX) {
				Log.d(TAG, "got: " + b);
			}

			Log.d(TAG, "found STX at pos=" + i);
			
			buffer[i++] = b; // STX

			// the next byte must be the message ID 
			if ((b = (byte)inStream.read()) != MSGID) {
				Log.d(TAG, "hm, next byte is not MSGID: " + b + " != " + MSGID);
				continue;
			}

			buffer[i++] = b; // MSGID

			// the next byte must be the data length, 
			// but for our current sensor it's always the same
			if ((b = (byte)inStream.read()) != DLC) {
				continue;
			}

			buffer[i++] = b; // DLC

			// read payload in bulk
			int cnt;
			if ((cnt = inStream.read(buffer, i, DLC)) < 0) {
				break;
			}
			i += cnt;

			// read until the end of text marker
			while ((b = (byte)inStream.read()) != ETX) {
				buffer[i++] = b;                                                         
			}

			buffer[i++] = b;
			
			break;
		}
		
		if (D) Log.d(TAG, "readNextData - read " + i + " bytes");
		
		return new String(buffer);
	}

	@Override
	public void setInputStream(InputStream inStream) {
		this.inStream = inStream;
	}

	@Override
	public void setOutputStream(OutputStream outStream) {
		this.outStream = outStream;
		sendEnableGeneralDataPacket();
	}
	
	private void sendEnableGeneralDataPacket() {
		byte[] payload = new byte[]{1};
		byte[] buffer = ZephyrConstants.createMessage((byte)0x14, payload);
		Log.d(TAG, "Enable general data packet messages");
		try {
			outStream.write(buffer);
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendLifeSignal() {
		try {
			outStream.write(new byte[]{0});
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
