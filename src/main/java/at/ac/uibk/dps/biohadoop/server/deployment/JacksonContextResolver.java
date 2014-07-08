package at.ac.uibk.dps.biohadoop.server.deployment;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {
	private ObjectMapper objectMapper;

	public JacksonContextResolver() {
		this.objectMapper = new ObjectMapper().enableDefaultTyping();
	}

	public ObjectMapper getContext(Class<?> objectType) {
		return objectMapper;
	}
}
