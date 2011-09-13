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
