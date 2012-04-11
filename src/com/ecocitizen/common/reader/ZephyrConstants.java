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


public abstract class ZephyrConstants {

	public static final byte STX = 0x02;
	public static final byte ETX = 0x03;

	public static final byte MSG_SET_GENERAL_PACKET_TRANSMIT_STATE = 0x14;

	public static byte getCRC(byte[] buffer) {
		int crc = 0;

		for (int i = 0; i < buffer.length; ++i) {
			int ch = buffer[i];
			crc = (crc ^ ch);
			for (int j = 0; j < 8; ++j) {
				if ((crc & 1) > 0) {
					crc = (crc >> 1) ^ 0x8c;
				}
				else {
					crc = (crc >> 1);
				}
			}
		}

		return (byte)crc;
	}

	public static byte getCRC(String buffer) {
		return getCRC(buffer.getBytes());
	}

	public static byte[] createMessage(byte msgID, byte[] payload) {
		// STX + MSGID + DLC + ... + CRC + ETX
		byte[] message = new byte[payload.length + 5];
		message[0] = STX;
		message[1] = msgID;
		message[2] = (byte)payload.length;
		int j = 3;
		for (int i = 0; i < payload.length; ++i, ++j) {
			message[j] = payload[i];
		}
		message[j] = getCRC(payload);
		message[j+1] = ETX;
		return message;
	}

}
