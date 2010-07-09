package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class CO2Sentence implements Serializable {
	
	public float co2;
	
	public enum Co2Level {
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
	public Co2Level co2Level;
	public final float co2_min = 400;
	public final float co2_max = 600;
	
	public CO2Sentence(String str) {
		String[] cols = str.split(",");
		co2 = Float.parseFloat(cols[3]);

		if (co2 < co2_min) {
			co2Level = Co2Level.LMIN;
		} else if (co2 > co2_max) {
			co2Level = Co2Level.LMAX;
		} else {
			int co2_int = (int)((co2 - co2_min) / (co2_max - co2_min) * 10);
			switch (co2_int) {
			case 0: co2Level = Co2Level.LMIN; break;
			case 1: co2Level = Co2Level.L1; break;
			case 2: co2Level = Co2Level.L2; break;
			case 3: co2Level = Co2Level.L3; break;
			case 4: co2Level = Co2Level.L4; break;
			case 5: co2Level = Co2Level.L5; break;
			case 6: co2Level = Co2Level.L6; break;
			case 7: co2Level = Co2Level.L7; break;
			case 8: co2Level = Co2Level.L8; break;
			default: co2Level = Co2Level.LMAX; break;
			}
		}
	}
}
