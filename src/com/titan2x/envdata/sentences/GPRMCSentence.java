package com.titan2x.envdata.sentences;

import java.io.Serializable;

public class GPRMCSentence implements Serializable {
	private static final long serialVersionUID = 3937903951172274070L;

	public float utctimeFLOAT;
	public char statusAV;
	public float latitude;
	public char latitudeNS;
	public float longitude;
	public char longitudeEW;
	public float groundSpeedKnots;
	public float trackDegrees;
	public float utcdateFLOAT;
	public String checksum;

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
		this.utctimeFLOAT = Float.parseFloat(cols[1]);
		this.statusAV = cols[2].charAt(0);
		this.latitude = Float.parseFloat(cols[3]);
		this.latitudeNS = cols[4].charAt(0);
		this.longitude = Float.parseFloat(cols[5]);
		this.longitudeEW = cols[6].charAt(0);
		this.groundSpeedKnots = Float.parseFloat(cols[7]);
		this.trackDegrees = Float.parseFloat(cols[8]);
		this.utctimeFLOAT = Float.parseFloat(cols[9]);
		this.checksum = cols[cols.length-1].substring(2);
	}
}
