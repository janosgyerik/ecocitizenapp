/**
 * 
 */
package com.titan2x.envdata.formats;

/**
 * @author janosgyerik
 *
 */

public class Format_1_GPS_CO2 {
	public String datestr;
	public String gpsType;
	public String gpsFlag;
	public float lat;
	public float lon;
	public float co2;
	
	public Format_1_GPS_CO2(String gpsLine, String co2Line) {
		String[] gpsCols = gpsLine.split(",");
		String[] co2Cols = co2Line.split(",");
		String ddmmyy = gpsCols[9];
		datestr = "20" + ddmmyy.substring(4, 6) + ddmmyy.substring(2,4) + ddmmyy.substring(0,2);
		gpsType = gpsCols[0];
		gpsFlag = gpsCols[2];
		lat = Float.parseFloat(gpsCols[3]);
		lon = Float.parseFloat(gpsCols[5]);
		co2 = Float.parseFloat(co2Cols[3]);
	}
	
	public String toString() {
		String str = "";
		str += datestr + ",";
		str += gpsType + ",";
		str += gpsFlag + ",";
		str += lat + ",";
		str += lon + ",";
		str += co2;
		return str + "\n";
	}
}
