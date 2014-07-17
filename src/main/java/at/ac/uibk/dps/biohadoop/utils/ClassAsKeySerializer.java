package at.ac.uibk.dps.biohadoop.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ClassAsKeySerializer extends JsonSerializer<Class> {

	@Override
	public void serialize(Class value,
			JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeFieldName(value.getCanonicalName());
	}

}
