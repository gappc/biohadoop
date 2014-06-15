package at.ac.uibk.dps.biohadoop.server.deployment;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.Path;

public class DeployingClasses {

	private final static Map<Class<?>, Class<?>> restfulClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
	private final static Map<Class<?>, Class<?>> webSocketClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
	
	public static boolean addRestfulClass(Class<?> restfulClass) {
		if (isRestful(restfulClass)) {
			restfulClasses.put(restfulClass, restfulClass);
			return true;
		}
		return false;
	}
	
	public static boolean addWebSocketClass(Class<?> webSocketClass) {
		if (isWebSocket(webSocketClass)) {
			webSocketClasses.put(webSocketClass, webSocketClass);
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
		return new CopyOnWriteArrayList<>(restfulClasses.keySet());
	}

	public static List<Class<?>> getWebSocketClasses() {
		return new CopyOnWriteArrayList<>(webSocketClasses.keySet());
	}
}
