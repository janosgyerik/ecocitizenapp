package com.ecocitizen.common;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import android.util.Log;

import com.ecocitizen.common.parser.PsenSentenceParser;
import com.ecocitizen.common.parser.SensorDataParser;
import com.ecocitizen.common.reader.DeviceReader;
import com.ecocitizen.common.reader.SimpleSentenceReader;

public class DeviceHandlerFactory {
	// Debugging
	private static final String TAG = "DeviceHandlerFactory";

	public static final Class defaultReaderClass = SimpleSentenceReader.class;
	public static final Class defaultParserClass = PsenSentenceParser.class;
	
	private List<PatternSpec> idPatternsForReaders;
	private List<PatternSpec> namePatternsForReaders;
	
	private List<PatternSpec> idPatternsForParsers;
	private List<PatternSpec> namePatternsForParsers;

	// cached parsers per device id
	private Map<String, SensorDataParser> parsers =
		new HashMap<String, SensorDataParser>();
	
	// cached readers per device id
	private Map<String, DeviceReader> readers =
		new HashMap<String, DeviceReader>();
	
	private DeviceHandlerFactory() {
		idPatternsForReaders = new LinkedList<PatternSpec>();
		idPatternsForParsers = new LinkedList<PatternSpec>();
		namePatternsForReaders = new LinkedList<PatternSpec>();
		namePatternsForParsers = new LinkedList<PatternSpec>();
		
		try {
			ResourceBundle props = ResourceBundle.getBundle("com.ecocitizen.common.DeviceHandlerFactory");
			Set<String> names = new HashSet<String>();
			
			for (Enumeration<String> e = props.getKeys(); e.hasMoreElements(); ) {
				String item = e.nextElement();
				if (item.startsWith("device_")) {
					names.add(item.substring(0, item.indexOf('.')));
				}
			}
			
			for (String name : names) {
				String namePattern = null;
				try {
					namePattern = props.getString(name + ".name");
					if (namePattern.length() == 0) {
						namePattern = null;
					}
				}
				catch (Exception e) {
				}
				
				String idPattern = null;
				try {
					idPattern = props.getString(name + ".id");
					if (idPattern.length() == 0) {
						idPattern = null;
					}
				}
				catch (Exception e) {
				}
				
				String readerClassName = null;
				try {
					readerClassName = props.getString(name + ".reader");
					if (readerClassName.length() == 0) {
						readerClassName = null;
					}
				}
				catch (Exception e) {
				}
				
				String parserClassName = null;
				try {
					parserClassName = props.getString(name + ".parser");
					if (parserClassName.length() == 0) {
						parserClassName = null;
					}
				}
				catch (Exception e) {
				}
				
				if (idPattern != null) {
					if (readerClassName != null) {
						idPatternsForReaders.add(new PatternSpec(idPattern, readerClassName));
					}
					if (parserClassName != null) {
						idPatternsForParsers.add(new PatternSpec(idPattern, parserClassName));
					}
				}
				
				if (namePattern != null) {
					if (readerClassName != null) {
						namePatternsForReaders.add(new PatternSpec(namePattern, readerClassName));
					}
					if (parserClassName != null) {
						namePatternsForParsers.add(new PatternSpec(namePattern, parserClassName));
					}
				}
			}
			
			System.out.println("# idPatternsForReaders");
			for (PatternSpec spec : idPatternsForReaders) {
				System.out.println(spec.pattern + " -> " + spec.className);
			}
			System.out.println("# idPatternsForParsers");
			for (PatternSpec spec : idPatternsForParsers) {
				System.out.println(spec.pattern + " -> " + spec.className);
			}
			System.out.println("# namePatternsForReaders");
			for (PatternSpec spec : namePatternsForReaders) {
				System.out.println(spec.pattern + " -> " + spec.className);
			}
			System.out.println("# namePatternsForParsers");
			for (PatternSpec spec : namePatternsForParsers) {
				System.out.println(spec.pattern + " -> " + spec.className);
			}
		}
		catch (Exception e) {
		}
	}
		
	private static DeviceHandlerFactory instance = null;

	synchronized public static DeviceHandlerFactory getInstance() {
		if (instance == null) {
			instance = new DeviceHandlerFactory();
		}
		return instance;
	}

	public DeviceReader getReader(String deviceName, String deviceId) {
		if (readers.containsKey(deviceId)) {
			return readers.get(deviceId);
		}
		else {
			DeviceReader reader = createReader(deviceName, deviceId);
			readers.put(deviceId, reader);
			return reader;
		}
	}
	
	public SensorDataParser getParser(String deviceName, String deviceId) {
		if (parsers.containsKey(deviceId)) {
			return parsers.get(deviceId);
		}
		else {
			SensorDataParser parser = createParser(deviceName, deviceId);
			parsers.put(deviceId, parser);
			return parser;
		}
	}
	
	private Object createClassByName(String className) {
		try {
			Class classToLoad = Class.forName(className);
			return classToLoad.newInstance();
		} catch (ClassNotFoundException e) {
			Log.w(TAG, "ClassNotFoundException when creating class: " + className);
		} catch (IllegalAccessException e) {
			Log.w(TAG, "IllegalAccessException when creating class: " + className);
		} catch (InstantiationException e) {
			Log.w(TAG, "InstantiationException when creating class: " + className);
		}
		return null;
	}
	
	private String findReaderClassName(String deviceName, String deviceId) {
		for (PatternSpec spec : idPatternsForReaders) {
			if (deviceId.matches(spec.pattern)) {
				return spec.className;
			}
		}
		
		for (PatternSpec spec : namePatternsForReaders) {
			if (deviceName.matches(spec.pattern)) {
				return spec.className;
			}
		}
		
		return defaultReaderClass.getCanonicalName();
	}
	
	private DeviceReader createReader(String deviceName, String deviceId) {
		String className = findReaderClassName(deviceName, deviceId);
		return createReaderByClassName(className);
	}
	
	private DeviceReader createReaderByClassName(String className) {
		Object obj = createClassByName(className);
		return obj != null ? (DeviceReader)obj : new SimpleSentenceReader();
	}

	private String findParserClassName(String deviceName, String deviceId) {
		for (PatternSpec spec : idPatternsForParsers) {
			if (deviceId.matches(spec.pattern)) {
				return spec.className;
			}
		}
		
		for (PatternSpec spec : namePatternsForParsers) {
			if (deviceName.matches(spec.pattern)) {
				return spec.className;
			}
		}
		
		return defaultParserClass.getCanonicalName();
	}
	
	private SensorDataParser createParser(String deviceName, String deviceId) {
		String className = findParserClassName(deviceName, deviceId);
		return createParserByClassName(className);
	}

	private SensorDataParser createParserByClassName(String className) {
		Object obj = createClassByName(className);
		return obj != null ? (SensorDataParser)obj : new PsenSentenceParser();
	}

	class PatternSpec {
		final String pattern;
		final String className;
		PatternSpec(String pattern, String className) {
			this.pattern = pattern;
			this.className = className;
		}
	}
	
}
