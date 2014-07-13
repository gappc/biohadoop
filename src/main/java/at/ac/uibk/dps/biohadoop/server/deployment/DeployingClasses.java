package at.ac.uibk.dps.biohadoop.server.deployment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.Path;

public class DeployingClasses {

	private static final Map<Class<?>, Class<?>> RESTFUL_CLASSES = new ConcurrentHashMap<Class<?>, Class<?>>();
	private static final Map<Class<?>, Class<?>> WEBSOCKET_CLASSES = new ConcurrentHashMap<Class<?>, Class<?>>();
	
	private DeployingClasses() {
	}
	
	public static boolean addRestfulClass(Class<?> restfulClass) {
		if (isRestful(restfulClass)) {
			RESTFUL_CLASSES.put(restfulClass, restfulClass);
			return true;
		}
		return false;
	}
	
	public static boolean addWebSocketClass(Class<?> webSocketClass) {
		if (isWebSocket(webSocketClass)) {
			WEBSOCKET_CLASSES.put(webSocketClass, webSocketClass);
			return true;
		}
		return false;		
	}
	
	public static boolean isRestful(Class<?> restfulClass) {
		return restfulClass.getAnnotation(Path.class) != null;
	}
	
	public static boolean isWebSocket(Class<?> webSocketClass) {
		return webSocketClass.getAnnotation(ServerEndpoint.class) != null;
	}
	
	public static List<Class<?>> getRestfulClasses() {
		return new CopyOnWriteArrayList<>(RESTFUL_CLASSES.keySet());
	}

	public static List<Class<?>> getWebSocketClasses() {
		return new CopyOnWriteArrayList<>(WEBSOCKET_CLASSES.keySet());
	}
}
