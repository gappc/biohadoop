package at.ac.uibk.dps.biohadoop.torename;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectCloner {

	private static Logger LOG = LoggerFactory.getLogger(ObjectCloner.class);
	private static ObjectMapper objectMapper = new ObjectMapper();

	public static <T>T deepCopy(Object data, Class<T> type) {
		if (data == null) {
			return null;
		}

		try (ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
				InputStream is = new ByteArrayInputStream(os.toByteArray());) {
			objectMapper.writeValue(os, data);
			return objectMapper.readValue(is, type);
		} catch (IOException e) {
			LOG.error("Error while cloning: object={} ", data, e);
		}
		return null;
	}
}
