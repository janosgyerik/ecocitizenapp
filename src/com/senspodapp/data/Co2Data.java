package com.senspodapp.data;

import java.io.Serializable;

public class Co2Data extends SensorData implements Serializable {
	private static final long serialVersionUID = 2762248368761766763L;

	@Override
	public SensorDataType getType() {
		return SensorDataType.Co2Data;
	}

	public static String sentencePattern = "$PSEN,CO2,";

	public float ppm;
	
	protected static float[] levelboundaries = new float[]{ 300, 350, 400, 450, 500, 600, 750, 1000, 1500 };
	
	public Co2Data(String sentence) {
		this.sentence = sentence.substring(sentence.indexOf(sentencePattern));
		
		String[] cols = this.sentence.split(",");
		ppm = Float.parseFloat(cols[3]);
		setLevel(ppm);
	}

}
