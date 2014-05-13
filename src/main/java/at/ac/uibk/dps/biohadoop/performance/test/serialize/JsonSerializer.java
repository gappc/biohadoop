package at.ac.uibk.dps.biohadoop.performance.test.serialize;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer<T> {

	private static Logger LOG = LoggerFactory.getLogger(JsonSerializer.class);
	private ObjectMapper mapper = new ObjectMapper();
	
//	public T readObject(InputStream is, T clazz) {
//		try {
//			return mapper.readValue(is, clazz);
//		} catch (IOException e) {
//			LOG.error("Error while deserializing Object", e);
//		}
//	}
}
