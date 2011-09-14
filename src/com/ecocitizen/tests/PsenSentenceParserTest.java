package com.ecocitizen.tests;


import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.ecocitizen.common.parser.PsenSentenceParser;
import com.ecocitizen.common.parser.SensorData;
import com.ecocitizen.common.parser.SensorDataFilter;
import com.ecocitizen.common.parser.SensorDataParser;
import com.ecocitizen.common.parser.SensorDataType;

public class PsenSentenceParserTest {
	
	static final String CO2_LINE = "$PSEN,CO2,ppm, 0479";
	static final String COx_LINE = "$PSEN,COx,V,1.625";
	static final String NOx_LINE = "$PSEN,NOx,V,2.591";
	static final String Noise_LINE = "$PSEN,Noise,dB,039";
	static final String Humidity_LINE = "$PSEN,Hum,H,50.93,T,30.88";
	static final String Temperature_LINE = "$PSEN,Hum,H,50.93,T,30.88";
	static final String Battery_LINE = "$PSEN,Batt,V,3.97";
	static final String GPRMC_LINE = "$GPRMC,011514.127,V,8960.0000,N,00000.0000,E,0.00,0.00,280910,,,N*77";
	
	SensorDataParser parser;
	
	@Before
	public void setUp() {
		parser = new PsenSentenceParser();
	}
	
	@Test
	public void testFindAnyPsen() {
		assertTrue("There must be matching data in CO2_LINE", 
				! parser.getSensorData(CO2_LINE).isEmpty());
		assertTrue("There must be matching data in COx_LINE", 
				! parser.getSensorData(COx_LINE).isEmpty());
		assertTrue("There must be matching data in NOx_LINE", 
				! parser.getSensorData(NOx_LINE).isEmpty());
		assertTrue("There must be matching data NOISE_LINE", 
				! parser.getSensorData(Noise_LINE).isEmpty());
		assertTrue("There must be matching data HUMIDITY_LINE", 
				! parser.getSensorData(Humidity_LINE).isEmpty());
		assertTrue("There must be matching data BATTERY_LINE", 
				! parser.getSensorData(Battery_LINE).isEmpty());
		assertTrue("There must NOT be matching data GPRMC_LINE", 
				parser.getSensorData(GPRMC_LINE).isEmpty());
	}
	
	@Test
	public void testCO2Filter() {
		assertTrue("There must be matching data in CO2 line with no filter", 
				! parser.getSensorData(CO2_LINE).isEmpty());
		assertTrue("There must be matching data in CO2 line with CO2 filter", 
				! parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
		assertHasDataType("There must be CO2 data in CO2 line with CO2 filter",
				SensorDataType.CO2,
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.CO2)));
		assertTrue("There must NOT be matching data in CO2 line with non-CO2 filter", 
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.COx)).isEmpty());
		assertTrue("There must NOT be matching data in non-CO2 line with CO2 filter", 
				parser.getSensorData(COx_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
	}
	
	@Test
	public void testCOxFilter() {
		assertTrue("There must be matching data in COx line with no filter", 
				! parser.getSensorData(COx_LINE).isEmpty());
		assertTrue("There must be matching data in COx line with COx filter", 
				! parser.getSensorData(COx_LINE, new SensorDataFilter(SensorDataType.COx)).isEmpty());
		assertHasDataType("There must be COx data in COx line with COx filter",
				SensorDataType.COx,
				parser.getSensorData(COx_LINE, new SensorDataFilter(SensorDataType.COx)));
		assertTrue("There must NOT be matching data in COx line with non-COx filter", 
				parser.getSensorData(COx_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
		assertTrue("There must NOT be matching data in non-COx line with COx filter", 
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.COx)).isEmpty());
	}
	
	@Test
	public void testNOxFilter() {
		assertTrue("There must be matching data in NOx line with no filter", 
				! parser.getSensorData(NOx_LINE).isEmpty());
		assertTrue("There must be matching data in NOx line with NOx filter", 
				! parser.getSensorData(NOx_LINE, new SensorDataFilter(SensorDataType.NOx)).isEmpty());
		assertHasDataType("There must be NOx data in NOx line with NOx filter",
				SensorDataType.NOx,
				parser.getSensorData(NOx_LINE, new SensorDataFilter(SensorDataType.NOx)));
		assertTrue("There must NOT be matching data in NOx line with non-NOx filter", 
				parser.getSensorData(NOx_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
		assertTrue("There must NOT be matching data in non-NOx line with NOx filter", 
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.NOx)).isEmpty());
	}
	
	@Test
	public void testNoiseFilter() {
		assertTrue("There must be matching data in Noise line with no filter", 
				! parser.getSensorData(Noise_LINE).isEmpty());
		assertTrue("There must be matching data in Noise line with Noise filter", 
				! parser.getSensorData(Noise_LINE, new SensorDataFilter(SensorDataType.Noise)).isEmpty());
		assertHasDataType("There must be Noise data in Noise line with Noise filter",
				SensorDataType.Noise,
				parser.getSensorData(Noise_LINE, new SensorDataFilter(SensorDataType.Noise)));
		assertTrue("There must NOT be matching data in Noise line with non-Noise filter", 
				parser.getSensorData(Noise_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
		assertTrue("There must NOT be matching data in non-Noise line with Noise filter", 
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.Noise)).isEmpty());
	}
	
	@Test
	public void testHumidityFilter() {
		assertTrue("There must be matching data in Humidity line with no filter", 
				! parser.getSensorData(Humidity_LINE).isEmpty());
		assertTrue("There must be matching data in Humidity line with Humidity filter", 
				! parser.getSensorData(Humidity_LINE, new SensorDataFilter(SensorDataType.Humidity)).isEmpty());
		assertHasDataType("There must be Humidity data in Humidity line with Humidity filter",
				SensorDataType.Humidity,
				parser.getSensorData(Humidity_LINE, new SensorDataFilter(SensorDataType.Humidity)));
		assertTrue("There must NOT be matching data in Humidity line with non-Humidity filter", 
				parser.getSensorData(Humidity_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
		assertTrue("There must NOT be matching data in non-Humidity line with Humidity filter", 
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.Humidity)).isEmpty());
	}
	
	@Test
	public void testTemperatureFilter() {
		assertTrue("There must be matching data in Humidity line with no filter", 
				! parser.getSensorData(Humidity_LINE).isEmpty());
		assertTrue("There must be matching data in Humidity line with Temperature filter", 
				! parser.getSensorData(Humidity_LINE, new SensorDataFilter(SensorDataType.Temperature)).isEmpty());
		assertHasDataType("There must be Temperature data in Temperature line with Temperature filter",
				SensorDataType.Temperature,
				parser.getSensorData(Temperature_LINE, new SensorDataFilter(SensorDataType.Temperature)));
		assertTrue("There must NOT be matching data in Humidity line with non-Temperature filter", 
				parser.getSensorData(Humidity_LINE, new SensorDataFilter(SensorDataType.CO2)).isEmpty());
		assertTrue("There must NOT be matching data in non-Humidity line with Temperature filter", 
				parser.getSensorData(CO2_LINE, new SensorDataFilter(SensorDataType.Temperature)).isEmpty());
	}
	
	@Test
	public void testManyFilters() {
		assertTrue("There must be matching data in NOx line with NOx and COx filter", 
				! parser.getSensorData(NOx_LINE, new SensorDataFilter(SensorDataType.NOx, SensorDataType.COx)).isEmpty());
		assertTrue("There must NOT be matching data in NOx line with COx and Noise filter", 
				parser.getSensorData(NOx_LINE, new SensorDataFilter(SensorDataType.COx, SensorDataType.Noise)).isEmpty());
	}
	
	private void assertHasDataType(String assertMsg, SensorDataType dataType,
			List<SensorData> sensorData) {
		for (SensorData data : sensorData) {
			if (data.getDataType() == dataType) return;
		}
		assertTrue(assertMsg, false);
	}

}
