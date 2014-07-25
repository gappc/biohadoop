package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.util.HashMap;
import java.util.Map;

public class ResourcePath {

	private static final Map<String, Class<? extends SuperComputable>> PATH_FOR_REST = new HashMap<>();
	private static final Map<String, Class<? extends SuperComputable>> PATH_FOR_WEBSOCKET = new HashMap<>();

	public static void addRestEntry(String key,
			Class<? extends SuperComputable> clazz) {
		PATH_FOR_REST.put(key, clazz);
	}

	public static void addWebSocketEntry(String key,
			Class<? extends SuperComputable> clazz) {
		PATH_FOR_WEBSOCKET.put(key, clazz);
	}

	public static Class<? extends SuperComputable> getRestEntry(String key) {
		return PATH_FOR_REST.get(key);
	}

	public static Class<? extends SuperComputable> getWebSocketEntry(String key) {
		return PATH_FOR_WEBSOCKET.get(key);
	}
	
}
