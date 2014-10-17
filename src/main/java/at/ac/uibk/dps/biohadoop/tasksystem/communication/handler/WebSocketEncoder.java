package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Taken from https://github.com/EsotericSoftware/kryonetty/blob/master/src/com/
 * esotericsoftware/kryonetty/KryoChannelPipelineFactory.java
 * 
 * @author Christian Gapp
 *
 */
public class WebSocketEncoder extends OneToOneEncoder {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketEncoder.class);
	
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		if (msg instanceof DefaultHttpResponse) {
			LOG.debug("Upgrading to WebSocket");
			return msg;
		}
		else {
			String output = objectMapper.writeValueAsString(msg);
			LOG.debug("Encoding {}", output);
			TextWebSocketFrame frame = new TextWebSocketFrame(output);
			return frame;
		}
	}

}
