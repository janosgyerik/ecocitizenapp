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