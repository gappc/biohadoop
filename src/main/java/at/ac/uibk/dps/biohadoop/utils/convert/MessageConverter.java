package at.ac.uibk.dps.biohadoop.utils.convert;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.queue.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.queue.Task;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;

public class MessageConverter {

	private static ObjectMapper objectMapper = new ObjectMapper();
	private static Map<String, JavaType> types = new HashMap<>();

	static {
		SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(Task.class, ClassNameWrappedTask.class);
		objectMapper.registerModule(module);
	}
	
	public static <T> Message<T> getTypedMessageForMethod(String data,
			String methodName, int pos) throws ConversionException {
		try {
			JsonNode node = objectMapper.readTree(data);
			String messageType = ((TextNode) node.findValue("type")).asText();

			if (MessageType.SHUTDOWN.toString().equals(messageType)) {
				return objectMapper.readValue(node.traverse(), Message.class);
			}

			String className = ((TextNode) node.findValue("className"))
					.asText();

			String key = className + methodName + pos;

			JavaType javaType = types.get(key);

			if (javaType == null) {
				Class<?> remoteExecutableClass = Class.forName(className);

				Class<?> receiveClass = null;
				for (Method method : remoteExecutableClass.getDeclaredMethods()) {
					if (!method.isSynthetic()
							&& methodName.equals(method.getName())) {
						if (pos == -1) {
							receiveClass = method.getReturnType();
						} else {
							receiveClass = method.getParameterTypes()[pos];
						}
					}
				}

				javaType = objectMapper.getTypeFactory()
						.constructParametricType(Message.class, receiveClass);
				types.put(key, javaType);
			}

			return objectMapper.readValue(node.traverse(), javaType);
		} catch (IOException | ClassNotFoundException e) {
			throw new ConversionException("Could not convert to Message, data="
					+ data, e);
		}
	}

}
