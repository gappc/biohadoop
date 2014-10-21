package at.ac.uibk.dps.biohadoop.utils.convert;


public class MessageConverter {

//	private static final ObjectMapper objectMapper = new ObjectMapper();
//	private static final Map<TaskTypeId, JavaType> types = new HashMap<>();
//
//	private static final String TYPE_NODE_NAME = "type";
//	private static final String TASK_TYPE_ID_NODE_NAME = "taskTypeId";
//	private static final String TASK_CONFIGURATION = "data";
//
//	static {
//		SimpleModule module = new SimpleModule();
//		// module.addAbstractTypeMapping(Task.class,
//		// ClassNameWrappedTask.class);
//		objectMapper.registerModule(module);
//	}
//
//	public static <T> Message<T> getTypedMessageForMethod(String data,
//			String methodName, int pos) throws ConversionException {
//		return null;
//		try {
//			JsonNode node = objectMapper.readTree(data);
//			MessageType messageType = getMessageType(node);
//
//			switch (messageType) {
//			case REGISTRATION_RESPONSE:
//				return convertRegistrationResponse(node, methodName, pos);
//			case SHUTDOWN:
//				return convertShutdown(node);
//			case WORK_INIT_RESPONSE:
//				return convertWorkInitResponse(node);
//			case WORK_RESPONSE:
//				throw new ConversionException(
//						"MessageType not supported, messageType=" + messageType);
//			default:
//				throw new ConversionException(
//						"MessageType not supported, messageType=" + messageType);
//			}

			// String asyncComputableClassName = ((TextNode) node
			// .findValue("className")).asText();
			//
			// String key = asyncComputableClassName + methodName + pos;
			//
			// JavaType javaType = types.get(key);
			//
			// if (javaType == null) {
			// Class<?> asyncComputableClass = Class
			// .forName(asyncComputableClassName);
			//
			// Class<?> receiveClass = null;
			// for (Method method : asyncComputableClass.getDeclaredMethods()) {
			// if (!method.isSynthetic()
			// && methodName.equals(method.getName())) {
			// if (pos == -1) {
			// receiveClass = method.getReturnType();
			// } else {
			// receiveClass = method.getParameterTypes()[pos];
			// }
			// }
			// }
			//
			// javaType = objectMapper.getTypeFactory()
			// .constructParametricType(Message.class, receiveClass);
			// types.put(key, javaType);
			// }
			//
			// return null;
//		} catch (IOException | ClassNotFoundException e) {
//			throw new ConversionException("Could not convert to Message, data="
//					+ data, e);
//		}
//	}
//
//	private static MessageType getMessageType(JsonNode node)
//			throws ConversionException {
//		try {
//			String messageTypeAsString = ((TextNode) node
//					.findValue(TYPE_NODE_NAME)).asText();
//			return MessageType.valueOf(messageTypeAsString);
//		} catch (Exception e) {
//			throw new ConversionException(
//					"Could not convert MessageType, type="
//							+ node.findValue(TYPE_NODE_NAME), e);
//		}
//	}
//
//	private static <T> Message<T> convertRegistrationResponse(JsonNode node,
//			String methodName, int pos) throws JsonParseException,
//			JsonMappingException, IOException, ConversionException, ClassNotFoundException {
//		JsonNode dataNode = node.findValue("data");
//		
//		String asyncComputableClassName = convertNode(dataNode, "asyncComputableClassName", String.class);
//		Class<?> asyncComputableClass = Class.forName(asyncComputableClassName);
//		Class<?> receiveClass = null;
//		for (Method method : asyncComputableClass.getDeclaredMethods()) {
//			if (!method.isSynthetic() && methodName.equals(method.getName())) {
//				if (pos == -1) {
//					receiveClass = method.getReturnType();
//				} else {
//					receiveClass = method.getParameterTypes()[pos];
//				}
//			}
//		}
//		
//		TaskTypeId taskTypeId = convertNode(dataNode, "taskTypeId", TaskTypeId.class);
//		
//		JavaType taskConfigurationType = objectMapper.getTypeFactory().constructParametricType(TaskConfiguration.class, receiveClass);
//		JavaType finalType = objectMapper.getTypeFactory().constructParametricType(Message.class, taskConfigurationType);
//		types.put(taskTypeId, finalType);
//		
//		JsonNode initialDataNode = dataNode.findValue("initialData");
//		return objectMapper.readValue(node.traverse(), finalType);
		// String key = asyncComputableClassName + methodName + pos;
		//
		// JavaType javaType = types.get(key);
		//
		// if (javaType == null) {
		// Class<?> asyncComputableClass = Class
		// .forName(asyncComputableClassName);
		//
		// Class<?> receiveClass = null;
		// for (Method method : asyncComputableClass.getDeclaredMethods()) {
		// if (!method.isSynthetic()
		// && methodName.equals(method.getName())) {
		// if (pos == -1) {
		// receiveClass = method.getReturnType();
		// } else {
		// receiveClass = method.getParameterTypes()[pos];
		// }
		// }
		// }
		//
		// javaType = objectMapper.getTypeFactory()
		// .constructParametricType(Message.class, receiveClass);
		// types.put(key, javaType);
		// }
		//
		// return objectMapper.readValue(node.traverse(), javaType);
		// } catch (IOException | ClassNotFoundException e) {
		// throw new ConversionException("Could not convert to Message, data="
		// + data, e);
		// }
//	}
//
//	@SuppressWarnings("unchecked")
//	private static <T> Message<T> convertShutdown(JsonNode node)
//			throws IOException, JsonParseException, JsonMappingException {
//		return objectMapper.readValue(node.traverse(), Message.class);
//	}
//
//	private static <T> Message<T> convertWorkInitResponse(JsonNode node)
//			throws JsonParseException, JsonMappingException, IOException,
//			ConversionException {
//		TaskTypeId taskTypeId = convertNode(node, TASK_TYPE_ID_NODE_NAME,
//				TaskTypeId.class);
//
//		JavaType javaType = types.get(taskTypeId);
//		if (javaType == null) {
//			return objectMapper.readValue(node.traverse(), Message.class);
//		} else {
//			return objectMapper.readValue(node.traverse(), javaType);
//		}
//	}
//
//	private static <T> T convertNode(JsonNode node, String nodeName,
//			Class<T> clazz) throws JsonParseException, JsonMappingException,
//			IOException, ConversionException {
//		JsonNode taskTypeIdNode = node.findValue(nodeName);
//		if (taskTypeIdNode == null) {
//			throw new ConversionException("Could not find node " + nodeName);
//		}
//		return objectMapper.readValue(taskTypeIdNode.traverse(), clazz);
//	}

}
