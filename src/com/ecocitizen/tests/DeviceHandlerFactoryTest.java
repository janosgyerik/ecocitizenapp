package com.ecocitizen.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ecocitizen.common.DeviceHandlerFactory;
import com.ecocitizen.common.reader.DeviceReader;
import com.ecocitizen.common.reader.SimpleSentenceReader;
import com.ecocitizen.common.reader.ZephyrGeneralDataReader;
import com.ecocitizen.common.reader.ZephyrHxmReader;

public class DeviceHandlerFactoryTest {
	
	static DeviceHandlerFactory factory;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		factory = DeviceHandlerFactory.getInstance();
	}
	
	@Test
	public void testSensarisSensor() {
		DeviceReader reader;
		reader = factory.getReader("SENSPOD_0054", "00:07:80:95:58:19");
		assertEquals(SimpleSentenceReader.class, reader.getClass());
		reader = factory.getReader("SENSPOD_3002", "00:07:80:93:54:5b");
		assertEquals(SimpleSentenceReader.class, reader.getClass());
	}
	
	@Test
	public void testZephyrSensor() {
		DeviceReader reader = factory.getReader("HXM004323", "00:07:80:9b:05:b2");
		assertEquals(ZephyrHxmReader.class, reader.getClass());
	}
	
	@Test
	public void testZephyrBioHarness() {
		DeviceReader reader = factory.getReader("BH ZBH001234", "00:07:80:9b:ff:ff");
		assertEquals(ZephyrGeneralDataReader.class, reader.getClass());
	}
	
	@Test
	public void testCommonReader() {
		DeviceReader reader = factory.getReader("TEST3", "00:00:00:00:00:03");
		assertEquals(CommonReader.class, reader.getClass());
		assertNotSame(SpecializedReader.class, reader.getClass());
	}
	
	@Test
	public void testSpecializedReader() {
		DeviceReader reader = factory.getReader("TEST5", "00:00:00:00:00:05");
		assertEquals(SpecializedReader.class, reader.getClass());
		assertNotSame(CommonReader.class, reader.getClass());
	}
	
	@Test
	public void testBorkedReader() {		
		DeviceReader reader = factory.getReader("BORKED", "00:00:00:00:00:ff");
		assertEquals(DeviceHandlerFactory.defaultReaderClass, reader.getClass());
		assertNotSame(CommonReader.class, reader.getClass());
	}
	
	@Test
	public void testOtherSensor() {
		DeviceReader reader = factory.getReader("blah1", "00:07:80:ff:ff:ff");
		assertEquals(DeviceHandlerFactory.defaultReaderClass, reader.getClass());
		assertNotSame(CommonReader.class, reader.getClass());
	}
	
	@Test
	public void testRegex() {
		assertTrue("SENSPOD_0054".matches("^SENSPOD_[0-9]+"));
		assertTrue("SENSPOD_0054".matches("^SENSPOD_[0-9]{4}"));
		assertTrue(! "ENSPOD_0054".matches("^SENSPOD_[0-9]+"));
		assertTrue(! "SENSPOD_x0054".matches("^SENSPOD_[0-9]+"));
	}
	
}
