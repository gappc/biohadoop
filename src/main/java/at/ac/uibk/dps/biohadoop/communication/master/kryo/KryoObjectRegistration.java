package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskId;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapper;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

public class KryoObjectRegistration {

	private static final List<Class<? extends Serializable>> OBJECTS = new CopyOnWriteArrayList<>(
			Arrays.asList(String[].class, Class.class, ClassNameWrapper.class, Message.class, MessageType.class, Object[].class,
					double[][].class, double[].class, int[].class, Task.class,
					TaskId.class, Double[][].class, Double[].class));
	private static final Map<Class<?>, Serializer<?>> OBJECTS_WITH_SERIALIZER = new ConcurrentHashMap<>();

	static {
		OBJECTS_WITH_SERIALIZER.put(UUID.class, new UUIDSerializer());
	}

	private KryoObjectRegistration() {
	}

	public static void register(Kryo kryo) {
		for (Class<?> type : OBJECTS) {
			kryo.register(type);
		}
		for (Class<?> type : OBJECTS_WITH_SERIALIZER.keySet()) {
			kryo.register(type, OBJECTS_WITH_SERIALIZER.get(type));
		}
	}

	public static void addRegistration(Class<? extends Serializable> type) {
		OBJECTS.add(type);
	}

	public static void addRegistration(Class<? extends Serializable> type,
			Serializer<?> serializer) {
		OBJECTS_WITH_SERIALIZER.put(type, serializer);
	}
}
