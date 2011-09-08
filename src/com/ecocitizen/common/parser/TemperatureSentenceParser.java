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

public class TemperatureSentenceParser extends PsenSentenceParser {

	public TemperatureSentenceParser() {
		super("$PSEN,Hum,");
		levelBoundaries = new double[]{ 5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,85,90,95 };
	}
	
	@Override
	public boolean match(String line) {
		reset();
		int dataStartIndex = line.indexOf(pattern);
		if (dataStartIndex > -1) {
			String[] cols = line.substring(dataStartIndex).split(",");
			if (cols.length < 4) return false;
            
			name = "Temperature"; 
			unit = cols[4];
			try {
				floatValue = Float.parseFloat(cols[5]);
				strValue = String.valueOf(floatValue);
			} catch (NumberFormatException e) {
				strValue = cols[5];
			}
			setLevel();

			return true;
		} 
		else {
			return false;
		}
	}	
}
