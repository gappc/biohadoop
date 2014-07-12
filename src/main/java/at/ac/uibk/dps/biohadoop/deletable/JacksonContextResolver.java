package at.ac.uibk.dps.biohadoop.deletable;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import at.ac.uibk.dps.biohadoop.connection.Message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {
	private ObjectMapper defaultObjectMapper;
	private ObjectMapper typedObjectMapper;

	public JacksonContextResolver() {
		this.defaultObjectMapper = new ObjectMapper();
		this.typedObjectMapper = new ObjectMapper().enableDefaultTyping();
	}

	// TODO check if this is the best choice. The problem is: SolverData gets
	// annotated with type information. Jackson can't deserialize all of this
	// type annotated data (e.g. java.util.TimeZone)
	public ObjectMapper getContext(Class<?> objectType) {
//		if (objectType == Message.class) {
//			return typedObjectMapper;
//		}
		return defaultObjectMapper;
	}
}
