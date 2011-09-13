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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DebugFlagManager {
	
	private Map<String, Boolean> debugFlagMap;
	
	private DebugFlagManager() {
		debugFlagMap = new HashMap<String, Boolean>();
		try {
			ResourceBundle props = ResourceBundle.getBundle("com.ecocitizen.common.DebugFlagManager");
			for (Enumeration<String> e = props.getKeys(); e.hasMoreElements(); ) {
				String className = e.nextElement();
				debugFlagMap.put(className, props.getString(className).equalsIgnoreCase("true"));
			}
		}
		catch (Exception e) {
			// Do nothing. Debug flag will be FALSE always.
		}
	}
	
	private static DebugFlagManager instance = null;

	synchronized public static DebugFlagManager getInstance() {
		if (instance == null) {
			instance = new DebugFlagManager();
		}
		return instance;
	}

	public boolean getDebugFlag(Class<?> cl) {
		Boolean flag = debugFlagMap.get(cl.getName());
		return flag != null ? flag : false;
	}

}