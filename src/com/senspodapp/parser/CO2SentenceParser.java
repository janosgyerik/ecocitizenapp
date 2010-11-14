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

public class CO2SentenceParser extends PsenSentenceParser {
	
	private static int[] levelboundaries = new int[]{ 300, 350, 400, 450, 500, 600, 750, 1000, 1500 };
	public int level = 0;
	public CO2SentenceParser() {
		super("$PSEN,CO2,");
	}
	public int getLevel(float ppm) {
		for (level = 0; level < levelboundaries.length; ++level) {
			if (ppm < levelboundaries[level]) break;
		}
	    return level;
	}
}

