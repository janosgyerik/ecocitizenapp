package com.titan2x.android.senspod;

import java.util.Date;

/**
 * 
 * @author janos
 * Collection of utility methods
 */
public abstract class Util {
    public static float convertNmeaToGps(float nmea) {
        return (int)(nmea / 100) + (nmea % 100) / 60;
    }
    
    public static Date getCurrentTimestamp() {
    	return new Date();
    }
}
