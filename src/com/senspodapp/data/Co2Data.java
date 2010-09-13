package com.senspodapp.data;

import java.io.Serializable;

public class Co2Data extends SensorData implements Serializable {
	private static final long serialVersionUID = 2762248368761766763L;

	public float ppm;
	
	protected static float[] levelboundaries = new float[]{ 300, 350, 400, 450, 500, 600, 750, 1000, 1500 };
	
	@Override
	public void initFromSentence(String sentence) {
		String[] cols = sentence.split(",");
		ppm = Float.parseFloat(cols[3]);
		setLevel(ppm);
	}
}
