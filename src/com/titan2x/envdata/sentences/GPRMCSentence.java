package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class GPRMCSentence implements Serializable {
	private static final long serialVersionUID = 3937903951172274070L;
	
	public String datestr;
	public String timestr;
	public String datetimestr;
	public String gpsFlag;
	public float lat;
	public float lon;

	/**
	 * Generate Sentence object from GPRMC Sentence.
	 * Example: $GPRMC,050759.130,V,3603.0309,N,14007.1527,E,0.06,0.00,030610,,,N*77
	 *          $GPRMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,ddmmyy,x.x,a*hh
	 * 1    = UTC of position fix
	 * 2    = Data status (V=navigation receiver warning)
	 * 3    = Latitude of fix
	 * 4    = N or S
	 * 5    = Longitude of fix
	 * 6    = E or W
	 * 7    = Speed over ground in knots
	 * 8    = Track made good in degrees True
	 * 9    = UT date
	 * 10   = Magnetic variation degrees (Easterly var. subtracts from true course)
	 * 11   = E or W
	 * 12   = Checksum
	 * @param str
	 */
	public GPRMCSentence(String str) {
		String[] cols = str.split(",");
		String ddmmyy = cols[9];
		datestr = "20" + ddmmyy.substring(4, 6) + ddmmyy.substring(2,4) + ddmmyy.substring(0,2);
		timestr = cols[1];
		datetimestr = datestr + '.' + timestr;
		String[] gpsFlagCols = cols[12].split("\\*");
		gpsFlag = gpsFlagCols[0];
		lat = Float.parseFloat(cols[3]);
		lon = Float.parseFloat(cols[5]);
	}
}
