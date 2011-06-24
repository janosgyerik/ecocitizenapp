/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
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

package com.ecocitizen.common;

public abstract class MessageType {
	public final static int SM_CONNECTION_FAILED = 11;
	public final static int SM_DEVICE_ADDED = 12;
	public final static int SM_DEVICE_CLOSED = 13;
	public final static int SM_DEVICE_LOST = 14;
	public final static int SM_ALL_DEVICES_GONE = 15;

	public final static int SENTENCE = 21;
	
	public final static int COMMENT = 31;

}
