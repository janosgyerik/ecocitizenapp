package com.ecocitizen.common.parser;

import java.util.HashSet;
import java.util.Set;

public class SensorDataFilter {
	
	public Set<SensorDataType> dataTypes;

	public SensorDataFilter() {
		dataTypes = new HashSet<SensorDataType>();
	}
	
	public SensorDataFilter(SensorDataType... dataTypes) {
		this();
		for (SensorDataType dataType : dataTypes) {
			this.dataTypes.add(dataType);
		}
	}

}
