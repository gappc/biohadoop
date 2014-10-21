package at.ac.uibk.dps.biohadoop.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectCloner {

	private static final Logger LOG = LoggerFactory.getLogger(ObjectCloner.class);
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enableDefaultTyping();

	private ObjectCloner() {
	}
	
	public static <T>T deepCopy(Object data, Class<T> type) {
		if (data != null) {
			try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
				OBJECT_MAPPER.writeValue(os, data);
				try (InputStream is = new ByteArrayInputStream(os.toByteArray());) {
					return OBJECT_MAPPER.readValue(is, type);
				} catch (IOException e) {
					LOG.error("Error while cloning: object={} ", data, e);
				}
			} catch (IOException e) {
				LOG.error("Error while cloning: object={} ", data, e);
			}
		}
		return null;
	}
}
