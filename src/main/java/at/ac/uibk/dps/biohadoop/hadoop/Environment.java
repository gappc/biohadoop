package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Environment {

	public static final String DEFAULT_PREFIX = "DEFAULT_PREFIX";
	public static final String HTTP_HOST = "HTTP_HOST";
	public static final String HTTP_PORT = "HTTP_PORT";
	public static final String SOCKET_HOST = "SOCKET_HOST";
	public static final String SOCKET_PORT = "SOCKET_PORT";
	public static final String KRYO_SOCKET_HOST = "KRYO_SOCKET_HOST";
	public static final String KRYO_SOCKET_PORT = "KRYO_SOCKET_PORT";

	private static final Map<String, String> environment = new ConcurrentHashMap<>();
	private static final AtomicBoolean SHUTDOWN = new AtomicBoolean(false);
	
	private static BiohadoopConfiguration biohadoopConfiguration;
	private static String biohadoopConfigurationPath;

	private Environment() {
	}

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

	public static String getBiohadoopConfigurationPath() {
		return biohadoopConfigurationPath;
	}

	public static void setBiohadoopConfigurationPath(
			String biohadoopConfigurationPath) {
		Environment.biohadoopConfigurationPath = biohadoopConfigurationPath;
	}
	
	public static void setShutdown() {
		SHUTDOWN.set(true);
	}
	
	public static boolean isShutdown() {
		return SHUTDOWN.get();
	}
}
