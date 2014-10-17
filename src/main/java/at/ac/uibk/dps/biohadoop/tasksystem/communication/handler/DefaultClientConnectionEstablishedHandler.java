package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;

public class DefaultClientConnectionEstablishedHandler extends
		SimpleChannelUpstreamHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultClientConnectionEstablishedHandler.class);

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		handleConnectionEstablished(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}

	protected void handleConnectionEstablished(ChannelHandlerContext ctx, ChannelEvent e)
			throws Exception {
		ctx.getPipeline().remove(this);
		LOG.info("Connection established");
		Message<?> message = new Message<>(MessageType.WORK_REQUEST, null);
		e.getChannel().write(message);
	}
}
