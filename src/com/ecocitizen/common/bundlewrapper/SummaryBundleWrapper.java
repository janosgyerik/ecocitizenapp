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

package com.ecocitizen.common.bundlewrapper;

import java.util.Formatter;

import android.os.Bundle;

import com.ecocitizen.common.Base64;

public class SummaryBundleWrapper extends AbstractBundleWrapper {
	
	public static final String MESSAGE_ID = "SUMMARY";
	
	private static final String BB_DTZ = "1";
	private static final String BB_SUMMARY = "2";
	
	private String dtz;
	private String summary;

	/**
	 * Use this constructor when extracting a Bundle received from somewhere.
	 * 
	 * @param bundle
	 */
	public SummaryBundleWrapper(Bundle bundle) {
		super(bundle);
		
		dtz = bundle.getString(BB_DTZ);
		summary = bundle.getString(BB_SUMMARY);
	}

	/**
	 * Convenience method to build a bundle from components
	 * 
	 * @param summary
	 * @return
	 */
	public static Bundle makeBundle(String summary) {
		Bundle bundle = new Bundle();
		bundle.putString(BB_DTZ, getCurrentDTZ());
		bundle.putString(BB_SUMMARY, summary);
		
		return bundle;
	}

	public String getSummary() {
		return summary;
	}
	
	public static String formatMessage(String dtz, String summary) {
		String datarecord = new Formatter().format(
				"%s,%s,%s,_",
				MESSAGE_ID,
				dtz,
				Base64.encodeBytes(summary.getBytes())
				).toString();
		return datarecord;
	}
	
	public static String formatMessage(String summary) {
		return formatMessage(getCurrentDTZ(), summary);
	}
	
	public String toString() {
		return formatMessage(dtz, getSummary());
	}

}
