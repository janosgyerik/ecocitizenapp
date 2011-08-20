package com.ecocitizen.common;

import java.util.HashMap;
import java.util.Map;

import com.ecocitizen.app.TreeViewActivity;

public class DebugFlagManager {
	
	private Map<Class<?>, Boolean> debugFlagMap;
	
	private DebugFlagManager() {
		debugFlagMap = new HashMap<Class<?>, Boolean>();
		debugFlagMap.put(TreeViewActivity.class, Boolean.TRUE);
	}
	
	private static DebugFlagManager instance = null;

	synchronized public static DebugFlagManager getInstance() {
		if (instance == null) {
			instance = new DebugFlagManager();
		}
		return instance;
	}

	public boolean getDebugFlag(Class<?> cl) {
		Boolean flag = debugFlagMap.get(cl);
		return flag != null ? flag : false;
	}

}
