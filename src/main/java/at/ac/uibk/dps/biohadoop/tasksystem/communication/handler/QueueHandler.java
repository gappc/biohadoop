package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;

public class QueueHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(QueueHandler.class);
	
	private final String pipelineName;
	
	public QueueHandler(String pipelineName) {
		this.pipelineName = pipelineName;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message<?> input = (Message<?>) e.getMessage();
//		LOG.debug("Handling message for pipeline {}", pipelineName);

		Message<Integer> message = new Message<>(
				MessageType.WORK_INIT_RESPONSE, null);

		e.getChannel().write(message);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}
}
