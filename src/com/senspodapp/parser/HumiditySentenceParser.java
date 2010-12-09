/*
 * Copyright (C) 2010 Mobile Environmental Sensing For Sustainable Cities
 *
 * This file is part of SenspodApp.
 *
 * SenspodApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SenspodApp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SenspodApp.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.senspodapp.parser;

public class HumiditySentenceParser extends PsenSentenceParser {
	
	private HumiditySentenceParser[] parsers = new HumiditySentenceParser[2];
	
	public HumiditySentenceParser() {
		super("$PSEN,Hum,");
		
	}
	
	private static HumiditySentenceParser parserHum = new HumiditySentenceParser();
	private static HumiditySentenceParser parserTem = new HumiditySentenceParser();
	
	@Override
	public boolean match(String line) {
		reset();
		int dataStartIndex = line.indexOf(pattern);
		if (dataStartIndex > -1) {
			String[] cols = line.substring(dataStartIndex).split(",");
			if (cols.length < 4) return false;
			
			name = cols[1];
			metric = cols[2];
			
			parserHum.name = cols[1];
			parserHum.metric = cols[2];
			
			parserTem.name = cols[4];
			parserTem.metric = "N.A";
			
			try {
			
				floatValue = Float.parseFloat(cols[3]);
				strValue = String.valueOf(floatValue);
				
				parserHum.floatValue = floatValue;
				parserHum.strValue = strValue;
				
				parserTem.floatValue = Float.parseFloat(cols[5]);
				parserTem.strValue = String.valueOf(parserTem.floatValue);
			
			} catch (NumberFormatException e) {
				strValue = cols[3];
				parserHum.strValue = cols[3];
				parserTem.strValue = cols[3];
			}
			
			parsers[0] = parserHum;
			parsers[1] = parserTem;
			setLevel();
			return true;
		} 
		else {
			return false;
		}
	}
	
	public 	HumiditySentenceParser[] getParsers() {
			
		return parsers;
	}

}