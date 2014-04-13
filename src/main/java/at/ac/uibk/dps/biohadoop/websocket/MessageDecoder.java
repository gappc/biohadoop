package at.ac.uibk.dps.biohadoop.websocket;

import java.io.IOException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MessageDecoder implements Decoder.Text<Message> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MessageDecoder.class);
	private ObjectMapper om = new ObjectMapper();

	@Override
	public void init(EndpointConfig config) {
		LOGGER.debug("MessageDecoder init");
	}

	@Override
	public void destroy() {
		LOGGER.debug("MessageDecoder destroy");
	}

	@Override
	public Message decode(String s) throws DecodeException {
		try {
			return om.readValue(s, Message.class);
		} catch (IOException e) {
			LOGGER.error("Error while Json decoding", e);
		}
		return null;
	}

	@Override
	public boolean willDecode(String s) {
		return true;
	}

}
