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

public class ZephyrGeneralDataReader implements DeviceReader {
	// Debugging
	private static final String TAG = "ZephyrGeneralDataReader";
	private static final boolean D = false;

	private InputStream inStream;
	private OutputStream outStream;
	
	private int lifeSignalCounter = 99;

	@Override
	public byte[] readNextData() throws IOException {
		byte[] buffer = new byte[255];
		byte b = 0;
		int i = 0;
		
		while (true) {
			if (lifeSignalCounter > 3) {
				lifeSignalCounter = 0;
				sendEnableGeneralDataPacket();
			}
			else {
				sendLifeSignal();
			}
			
			// skip until the message start marker
			while ((b = (byte)inStream.read()) != ZephyrConstants.STX) ;

			i = 0;
			buffer[i++] = b; // STX

			// the next byte must be the message ID 
			b = (byte)inStream.read();
			
			switch (b) {
			case ZephyrConstants.MSG_LIFE_SIGNAL:
				if (D) Log.d(TAG, "Received MSG_LIFE_SIGNAL");
				++lifeSignalCounter;
				continue;
			case ZephyrConstants.MSG_SET_GENERAL_DATA_PACKET_TRANSMIT_STATE:
				if (D) Log.d(TAG, "Received MSG_SET_GENERAL_DATA_PACKET_TRANSMIT_STATE");
				break;
			case ZephyrConstants.MSG_GENERAL_DATA_PACKET:
				if (D) Log.d(TAG, "Received MSG_GENERAL_DATA_PACKET");
				break;
			default:
				if (D) Log.d(TAG, "Received unknown message: " + b);
				continue;
			}
			lifeSignalCounter = 0;

			buffer[i++] = b; // MSGID

			int dlc = inStream.read(); // DLC
			buffer[i++] = (byte)dlc;

			// read payload in bulk
			int cnt;
			if ((cnt = inStream.read(buffer, i, dlc)) < 0) {
				break;
			}
			i += cnt;

			// read until the end of text marker
			while ((b = (byte)inStream.read()) != ZephyrConstants.ETX) {
				buffer[i++] = b;                                                         
			}

			buffer[i++] = b;
			
			if (D) Log.d(TAG, "Total bytes read = " + i);
			
			break;
		}

		byte[] compact = new byte[buffer.length];
		System.arraycopy(buffer, 0, compact, 0, compact.length);

		return compact;
	}

	@Override
	public void setInputStream(InputStream inStream) {
		this.inStream = inStream;
	}

	@Override
	public void setOutputStream(OutputStream outStream) {
		this.outStream = outStream;
	}
	
	private void sendEnableGeneralDataPacket() throws IOException {
		Log.d(TAG, "Enable general data packet messages");
		byte[] buffer = ZephyrConstants.createSetGeneralDataPacketTransmitEnabledMessage();
		outStream.write(buffer);
		outStream.flush();
	}
	
	private void sendLifeSignal() throws IOException {
		outStream.write(new byte[]{0});
		outStream.flush();
	}

	@Override
	public void initialize() {
		lifeSignalCounter = 99;
	}
}
