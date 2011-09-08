package com.ecocitizen.common.parser;

import java.util.HashSet;
import java.util.Set;

public class SensorDataFilter {
	
	public Set<SensorDataType> dataTypes;

	public SensorDataFilter() {
		dataTypes = new HashSet<SensorDataType>();
	}
	
	public SensorDataFilter(SensorDataType dataType) {
		this();
		dataTypes.add(dataType);
	}

	public SensorDataFilter(SensorDataType dataType1, SensorDataType dataType2) {
		this();
		dataTypes.add(dataType1);
		dataTypes.add(dataType2);
	}

}
