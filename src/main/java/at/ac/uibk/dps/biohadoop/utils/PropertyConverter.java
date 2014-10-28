package at.ac.uibk.dps.biohadoop.utils;

import java.util.Arrays;
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

	public static String toString(Map<String, String> properties, String key,
			String[] validVals) throws AlgorithmException {
		String value = properties.get(key);
		if (validVals == null) {
			return value;
		}
		for (String validVal : validVals) {
			if (validVal == null) {
				if (value == null) {
					return value;
				}
			} else {
				if (validVal.equals(value)) {
					return value;
				}
			}
		}

		throw new AlgorithmException("Property " + key
				+ " has invalid value. Valid values are: "
				+ Arrays.toString(validVals));
	}
}
