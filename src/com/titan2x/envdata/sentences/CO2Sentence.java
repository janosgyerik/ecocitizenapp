package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class CO2Sentence implements Serializable {
	private static final long serialVersionUID = 2762248368761766763L;

	public String str;
	
	public float ppm;
	
	public enum Level {
		LMIN,
		L1,
		L2,
		L3,
		L4,
		L5,
		L6,
		L7,
		L8,
		LMAX
	}
	public Level level;
	public final float co2_min = 400;
	public final float co2_max = 600;
	
	public CO2Sentence(String str) {
		this.str = str;
		
		String[] cols = str.split(",");
		ppm = Float.parseFloat(cols[3]);

		if (ppm < co2_min) {
			level = Level.LMIN;
		} else if (ppm > co2_max) {
			level = Level.LMAX;
		} else {
			int co2_int = (int)((ppm - co2_min) / (co2_max - co2_min) * 10);
			switch (co2_int) {
			case 0: level = Level.LMIN; break;
			case 1: level = Level.L1; break;
			case 2: level = Level.L2; break;
			case 3: level = Level.L3; break;
			case 4: level = Level.L4; break;
			case 5: level = Level.L5; break;
			case 6: level = Level.L6; break;
			case 7: level = Level.L7; break;
			case 8: level = Level.L8; break;
			default: level = Level.LMAX; break;
			}
		}
	}
}
