package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Environment {

	public final static String HTTP_HOST = "HTTP_HOST";
	public final static String HTTP_PORT = "HTTP_PORT";
	public final static String SOCKET_HOST = "SOCKET_HOST";
	public final static String SOCKET_PORT = "SOCKET_PORT";
	public final static String KRYO_SOCKET_HOST = "KRYO_SOCKET_HOST";
	public final static String KRYO_SOCKET_PORT = "KRYO_SOCKET_PORT";
	
	private static Map<String, String> environment = new ConcurrentHashMap<>();
	private static BiohadoopConfiguration biohadoopConfiguration;
	
	public static String get(String key) {
		return environment.get(key);
	}

	public static String set(String key, String value) {
		return environment.put(key, value);
	}
	
	public static String getPrefixed(String prefix, String key) {
		return environment.get(prefix + "_" + key);
	}
	
	public static String setPrefixed(String prefix, String key, String value) {
		return environment.put(prefix + "_" + key, value);
	}

	public static BiohadoopConfiguration getBiohadoopConfiguration() {
		return biohadoopConfiguration;
	}

	public static void setBiohadoopConfiguration(
			BiohadoopConfiguration biohadoopConfiguration) {
		Environment.biohadoopConfiguration = biohadoopConfiguration;
	}

}
