package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class CO2Sentence implements Serializable {
	private static final long serialVersionUID = 2762248368761766763L;

	public String sentence = null; 
	
	public float ppm;
	
	private static int[] levelboundaries = new int[]{ 300, 350, 400, 450, 500, 600, 750, 1000, 1500 };
	public int level = 0;
	
	public CO2Sentence(String str) {
		sentence = str;
		String[] cols = str.split(",");
		ppm = Float.parseFloat(cols[3]);
		
		for (level = 0; level < levelboundaries.length; ++level) {
			if (ppm < levelboundaries[level]) break;
		}
	}
}
