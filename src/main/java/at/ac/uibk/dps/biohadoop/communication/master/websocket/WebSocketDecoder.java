package at.ac.uibk.dps.biohadoop.communication.master.websocket;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WebSocketDecoder implements Decoder.Text<Message<?>> {

	private static final Logger LOG = LoggerFactory.getLogger(WebSocketDecoder.class);
	private ObjectMapper om = new ObjectMapper();

	@Override
	public void init(EndpointConfig config) {
		LOG.debug("MessageDecoder init");
		om.enableDefaultTyping();
	}

	@Override
	public void destroy() {
		LOG.debug("MessageDecoder destroy");
	}

	@Override
	public Message<?> decode(String s) throws DecodeException {
		try {
			return om.readValue(s, Message.class);
		} catch (IOException e) {
			LOG.error("Error while Json decoding", e);
		}
		return null;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

}
