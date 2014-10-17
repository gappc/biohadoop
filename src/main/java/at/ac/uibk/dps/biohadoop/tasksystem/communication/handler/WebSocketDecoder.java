package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;

public class WebSocketDecoder extends OneToOneDecoder {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketDecoder.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		String input = ((TextWebSocketFrame) msg).getText();
		LOG.debug("Decoding {}", input);
		Message<?> message = objectMapper.readValue(input, Message.class);
		return message;
	}

}
