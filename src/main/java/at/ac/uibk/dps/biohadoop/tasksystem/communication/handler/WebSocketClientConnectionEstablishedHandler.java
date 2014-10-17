package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.event.HandshakeEvent;

public class WebSocketClientConnectionEstablishedHandler extends
		DefaultClientConnectionEstablishedHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketClientConnectionEstablishedHandler.class);

	/*
	 * Listens for underlying WebSocket handshake event (WebSocket upgrade),
	 * fired by WebSocketHandshakeHandler. After the handshake has completed,
	 * the decoder and encoder are added to the pipeline. Then, the initial
	 * message is sent to the server and this handler is removed from the
	 * pipeline, too
	 * 
	 * @see
	 * org.jboss.netty.channel.SimpleChannelUpstreamHandler#handleUpstream(org
	 * .jboss.netty.channel.ChannelHandlerContext,
	 * org.jboss.netty.channel.ChannelEvent)
	 */
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		if (e instanceof HandshakeEvent) {
			ctx.getPipeline().addAfter("handshake", "webSocketDecoder",
					new WebSocketDecoder());
			ctx.getPipeline().addAfter("handshake", "webSocketEncoder",
					new WebSocketEncoder());
			ctx.getPipeline().remove(WebSocketHandshakeHandler.class);
			super.handleConnectionEstablished(ctx, e);
		} else {
			LOG.debug("Event is not of type {}", HandshakeEvent.class);
		}
	}

}
