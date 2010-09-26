package com.senspodapp.data;

import java.io.Serializable;

public class AndroidGpsInfo extends GpsInfo implements Serializable {
	private static final long serialVersionUID = -6790095960624269945L;
	
	public float accuracy;
	public double altitude;
	public float speed;

	public AndroidGpsInfo(double lat, double lon, float accuracy, double altitude, float speed) {
		this.lat = lat;
		this.lon = lon;
		this.accuracy = accuracy;
		this.altitude = altitude;
		this.speed = speed;
	}

	@Override
	public SensorDataType getType() {
		return SensorDataType.AndroidGpsInfo;
	}

}
