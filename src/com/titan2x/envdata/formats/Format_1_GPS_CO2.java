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
	public String timestr;
	public String datetimestr;
	public String gpsType;
	public String gpsFlag;
	public float lat;
	public float lon;
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
	
	public Format_1_GPS_CO2(String gpsLine, String co2Line) {
		String[] gpsCols = gpsLine.split(",");
		String[] co2Cols = co2Line.split(",");
		String ddmmyy = gpsCols[9];
		datestr = "20" + ddmmyy.substring(4, 6) + ddmmyy.substring(2,4) + ddmmyy.substring(0,2);
		timestr = gpsCols[1];
		datetimestr = datestr + '.' + timestr;
		gpsType = gpsCols[0];
		String[] gpsFlagCols = gpsCols[12].split("\\*");
		gpsFlag = gpsFlagCols[0];
		lat = Float.parseFloat(gpsCols[3]);
		lon = Float.parseFloat(gpsCols[5]);
		co2 = Float.parseFloat(co2Cols[3]);
		
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

	public String toString() {
		String str = "";
		str += datetimestr + ",";
		str += gpsFlag + ",";
		str += lat + ",";
		str += lon + ",";
		str += co2 + ",";
		str += co2Level.ordinal();
		return str + "\n";
	}
}
