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

package com.ecocitizen.parser;

public class NOxSentenceParser extends PsenSentenceParser {
	static {
		levelBoundaries = new double[]{ 0.1,0.3,0.7,1.1,1.5,1.9,2.3,2.7,3.5 };
		//levelBoundaries = new double[]{ 0.1,0.3,0.5,0.7,0.9,1.1,1.3,1.5,1.7,1.9,2.1,2.3,2.5,2.7,2.9,3.1,3.3,3.5 };
	}

	public NOxSentenceParser() {
		super("$PSEN,NOx,");
	}
	
	public String getMetric() {
		return "ppm"; // TODO this is only temporary
	}
}
