package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class GPRMCSentence implements Serializable {
	
	public String datestr;
	public String timestr;
	public String datetimestr;
	public String gpsType;
	public String gpsFlag;
	public float lat;
	public float lon;

	public GPRMCSentence(String str) {
		String[] cols = str.split(",");
		String ddmmyy = cols[9];
		datestr = "20" + ddmmyy.substring(4, 6) + ddmmyy.substring(2,4) + ddmmyy.substring(0,2);
		timestr = cols[1];
		datetimestr = datestr + '.' + timestr;
		gpsType = cols[0];
		String[] gpsFlagCols = cols[12].split("\\*");
		gpsFlag = gpsFlagCols[0];
		lat = Float.parseFloat(cols[3]);
		lon = Float.parseFloat(cols[5]);
	}
}
