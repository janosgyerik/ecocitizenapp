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

package com.ecocitizen.common.bundlewrapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.os.Bundle;

public abstract class AbstractBundleWrapper {
	private final Bundle bundle;
	
	public AbstractBundleWrapper(Bundle bundle) {
		this.bundle = bundle;
	}
	
	public Bundle getBundle() {
		return bundle;
	}
	static final SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMddHHmmss.S");

	public static final String getCurrentDTZ() {
		return String.format("%s,%d", dtFormat.format(new Date()), 
				TimeZone.getDefault().getRawOffset() / 3600000);
	}
}
