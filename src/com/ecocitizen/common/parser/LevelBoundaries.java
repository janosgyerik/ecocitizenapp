package com.ecocitizen.common.parser;

import java.util.HashMap;
import java.util.Map;

public class LevelBoundaries {
	
	private static Map<SensorDataType, double[]> levelBoundariesMap;
	
	static {
		levelBoundariesMap = new HashMap<SensorDataType, double[]>();
		//levelBoundaries.put(SensorDataType.CO2, new double[]{ 300, 325, 350, 375, 400, 425, 450, 475, 500, 525, 550, 575, 600, 625, 650, 675, 700, 800, 1000 });
		levelBoundariesMap.put(SensorDataType.CO2, new double[]{ 300, 350, 400, 450, 500, 600, 700, 800, 1000 });
		//levelBoundaries.put(SensorDataType.COx, new double[]{ 0.1,0.3,0.5,0.7,0.9,1.1,1.3,1.5,1.7,1.9,2.1,2.3,2.5,2.7,2.9,3.1,3.3,3.5 });
		levelBoundariesMap.put(SensorDataType.COx, new double[]{ 0.1,0.3,0.7,1.1,1.5,1.9,2.3,2.7,3.5 });
		//levelBoundaries.put(SensorDataType.NOx, new double[]{ 0.1,0.3,0.5,0.7,0.9,1.1,1.3,1.5,1.7,1.9,2.1,2.3,2.5,2.7,2.9,3.1,3.3,3.5 });
		levelBoundariesMap.put(SensorDataType.NOx, new double[]{ 0.1,0.3,0.7,1.1,1.5,1.9,2.3,2.7,3.5 });
		levelBoundariesMap.put(SensorDataType.Noise, new double[]{ 5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,85,90,95 });
		levelBoundariesMap.put(SensorDataType.Humidity, new double[]{ 5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,85,90,95 });
		levelBoundariesMap.put(SensorDataType.Temperature, new double[]{ 5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,85,90,95 });
		levelBoundariesMap.put(SensorDataType.HeartRate, new double[]{ });
	}
	
	public static int getLevel(SensorDataType dataType, float value) {
		int level = 0;
		if (levelBoundariesMap.containsKey(dataType)) {
			double[] levelBoundaries = levelBoundariesMap.get(dataType);
			for (; level < levelBoundaries.length; ++level) {
				if (value < levelBoundaries[level]) break;
			}
		}
		return level;
	}

}
