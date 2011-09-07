package com.ecocitizen.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ecocitizen.common.DeviceHandlerFactory;
import com.ecocitizen.common.reader.DeviceReader;
import com.ecocitizen.common.reader.SimpleSentenceReader;
import com.ecocitizen.common.reader.ZephyrReader;

public class DeviceHandlerFactoryTest {
	
	static DeviceHandlerFactory factory;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		factory = DeviceHandlerFactory.getInstance();
	}
	
	@Test
	public void testSensarisSensor() {
		DeviceReader reader;
		reader = factory.createReader("SENSPOD_0054", "00:07:80:95:58:19");
		assertEquals(reader.getClass(), SimpleSentenceReader.class);
		reader = factory.createReader("SENSPOD_3002", "00:07:80:93:54:5b");
		assertEquals(reader.getClass(), SimpleSentenceReader.class);
	}
	
	@Test
	public void testZephyrSensor() {
		DeviceReader reader = factory.createReader("HXM004323", "00:07:80:9b:05:b2");
		assertEquals(reader.getClass(), ZephyrReader.class);
	}
	
	@Test
	public void testCommonReader() {
		DeviceReader reader = factory.createReader("TEST3", "00:00:00:00:00:00");
		assertEquals(reader.getClass(), CommonReader.class);
		assertNotSame(SpecializedReader.class, reader.getClass());
	}
	
	@Test
	public void testSpecializedReader() {
		DeviceReader reader = factory.createReader("TEST5", "00:00:00:00:00:00");
		assertEquals(reader.getClass(), SpecializedReader.class);
		assertNotSame(CommonReader.class, reader.getClass());
	}
	
	@Test
	public void testBorkedReader() {		
		DeviceReader reader = factory.createReader("BORKED", "00:00:00:00:00:00");
		assertEquals(reader.getClass(), DeviceHandlerFactory.defaultReaderClass);
		assertNotSame(CommonReader.class, reader.getClass());
	}
	
	@Test
	public void testOtherSensor() {
		DeviceReader reader = factory.createReader("blah1", "00:07:80:ff:ff:ff");
		assertEquals(reader.getClass(), DeviceHandlerFactory.defaultReaderClass);
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
