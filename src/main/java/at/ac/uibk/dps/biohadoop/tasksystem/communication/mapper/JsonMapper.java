package at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper;

import org.codehaus.jackson.map.ObjectMapper;

public class JsonMapper {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.enableDefaultTyping();
	}
}
