package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.SimpleTask;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;

public class KryoObjectRegistration {

	private static final List<Class<? extends Serializable>> DEFAULT_OBJECTS = new CopyOnWriteArrayList<>(
			Arrays.asList(char[].class, char[][].class, int[].class,
					int[][].class, float[].class, float[][].class,
					double[].class, double[][].class, Character[].class,
					Character[][].class, Integer[].class, Integer[][].class,
					Float[].class, Float[][].class, Double[].class,
					Double[][].class, String[].class, String[][].class,
					Object[].class, Class.class, TaskId.class,
					SimpleTask.class, ClassNameWrappedTask.class,
					Message.class, MessageType.class));

	private static final Map<Class<?>, Serializer<?>> DEFAULT_OBJECTS_WITH_SERIALIZER = new ConcurrentHashMap<>();

	static {
		DEFAULT_OBJECTS_WITH_SERIALIZER.put(UUID.class, new UUIDSerializer());
	}

	private KryoObjectRegistration() {
	}

	public static void registerDefaultObjects(Kryo kryo) {
		for (Class<?> type : DEFAULT_OBJECTS) {
			kryo.register(type);
		}
		for (Class<?> type : DEFAULT_OBJECTS_WITH_SERIALIZER.keySet()) {
			kryo.register(type, DEFAULT_OBJECTS_WITH_SERIALIZER.get(type));
		}
	}

	public static void registerType(Kryo kryo,
			Class<? extends Serializable> type) {
		kryo.register(type);
	}

	public static void registerTypes(Kryo kryo,
			List<Class<? extends Object>> types) {
		if (types != null) {
			for (Class<? extends Object> type : types) {
				kryo.register(type);
			}
		}
	}

	public static void registerTypes(Kryo kryo,
			Map<Class<? extends Object>, Serializer<?>> types) {
		if (types != null) {
			for (Class<? extends Object> type : types.keySet()) {
				kryo.register(type, types.get(type));
			}
		}
	}
}
