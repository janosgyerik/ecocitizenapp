package com.ecocitizen.common.parser;

import java.util.List;

public interface SensorDataParser {

	/**
	 * Parse the data bytes and return all data items.
	 * 
	 * Return empty list if there are no recognizable
	 * data items in the data bytes.
	 * 
	 * @param bytes
	 * @return
	 */
	List<SensorData> getSensorData(String bytes);

	/**
	 * Parse the data bytes and return data times that
	 * match the specified filters. 
	 * 
	 * If the device does not support any of the specific filters, 
	 * the implementation should skip processing.
	 * 
	 * @param bytes
	 * @param filters
	 * @return
	 */
	List<SensorData> getSensorData(String bytes, SensorDataFilter filter);

}
