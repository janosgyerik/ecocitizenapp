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

public class PsenSentenceParser {
	String metric;
	float floatValue;
	String strValue;
	String name;
	int level;

	double[] levelBoundaries = new double[]{};

	String pattern = "$PSEN,";

	public PsenSentenceParser(String pattern) {
		this.pattern = pattern;
	}

	void setLevel() {
		for (level = 0; level < levelBoundaries.length; ++level) {
			if (floatValue < levelBoundaries[level]) break;
		}
	}

	void reset() {
		metric = null;
		floatValue = Float.NaN;
		strValue = null;
		level = 0;
		name = null;
	}

	public PsenSentenceParser() {
		reset();
	}

	public boolean match(String line) {
		reset();
		int dataStartIndex = line.indexOf(pattern);
		if (dataStartIndex > -1) {
			String[] cols = line.substring(dataStartIndex).split(",");
			if (cols.length < 4) return false;

			name = cols[1];
			metric = cols[2];
			try {
				floatValue = Float.parseFloat(cols[3]);
				strValue = String.valueOf(floatValue);
			} catch (NumberFormatException e) {
				strValue = cols[3];
			}
			setLevel();

			return true;
		} 
		else {
			return false;
		}
	}

	public String getMetric() {
		return metric;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public String getStrValue() {
		return strValue;
	}

	public int getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}
}
