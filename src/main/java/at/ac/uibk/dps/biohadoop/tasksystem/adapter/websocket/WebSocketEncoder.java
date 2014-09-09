package at.ac.uibk.dps.biohadoop.tasksystem.adapter.websocket;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketEncoder<Message> implements Encoder.Text<Message> {
	
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketEncoder.class);
	private ObjectMapper om = new ObjectMapper();
	
	@Override
	public void init(EndpointConfig config) {
		LOG.debug("WebSocketEncoder init");
		om.enableDefaultTyping();
	}

	@Override
	public void destroy() {
		LOG.debug("WebSocketEncoder destroy");
	}

	@Override
	public String encode(Message object) throws EncodeException {
		try {
			return om.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			LOG.error("Error while encoding to Json", e);
		}
		return "JSON ERROR";
	}

}
