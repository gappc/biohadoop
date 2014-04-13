package at.ac.uibk.dps.biohadoop.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketEncoder<T> implements Encoder.Text<T> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEncoder.class);
	private ObjectMapper om = new ObjectMapper();
	
	@Override
	public void init(EndpointConfig config) {
		LOGGER.debug("WebSocketEncoder init");
	}

	@Override
	public void destroy() {
		LOGGER.debug("WebSocketEncoder destroy");
	}

	@Override
	public String encode(T object) throws EncodeException {
		try {
			return om.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			LOGGER.error("Error while encoding to Json", e);
		}
		return "JSON ERROR";
	}


}
