package at.ac.uibk.dps.biohadoop.utils;

import java.util.HashMap;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.communication.master.Master;

public class ResourcePath {

	private static final Map<String, Class<? extends Master>> PATH_FOR_REST = new HashMap<>();
	private static final Map<String, Class<? extends Master>> PATH_FOR_WEBSOCKET = new HashMap<>();

	public static void addRestEntry(String key,
			Class<? extends Master> clazz) {
		PATH_FOR_REST.put(key, clazz);
	}

	public static void addWebSocketEntry(String key,
			Class<? extends Master> clazz) {
		PATH_FOR_WEBSOCKET.put(key, clazz);
	}

	public static Class<? extends Master> getRestEntry(String key) {
		return PATH_FOR_REST.get(key);
	}

	public static Class<? extends Master> getWebSocketEntry(String key) {
		return PATH_FOR_WEBSOCKET.get(key);
	}
	
}
