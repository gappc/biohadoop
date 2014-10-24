package at.ac.uibk.dps.biohadoop.utils;

import java.util.Map;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmException;

public class PropertyConverter {

	public static int toInt(Map<String, String> properties, String key)
			throws AlgorithmException {
		String value = null;
		try {
			value = properties.get(key);
			return Integer.parseInt(value);
		} catch (Exception e) {
			throw new AlgorithmException("Could not convert property " + key
					+ " to int, value was " + value, e);
		}
	}
	
	public static long toLong(Map<String, String> properties, String key)
			throws AlgorithmException {
		String value = null;
		try {
			value = properties.get(key);
			return Long.parseLong(value);
		} catch (Exception e) {
			throw new AlgorithmException("Could not convert property " + key
					+ " to long, value was " + value, e);
		}
	}
}
