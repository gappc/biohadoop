package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;

public class TestWorkerHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(TestWorkerHandler.class);

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
		// e.getChannel().write(inputMessage);

		Task<?> inputTask = inputMessage.getTask();
		Task<?> outputTask = new Task<>(inputTask.getTaskId(),
				inputTask.getTaskTypeId(), new Double(50000));
		Message outputMessage = new Message(
				MessageType.WORK_REQUEST.ordinal(), outputTask);
		e.getChannel().write(outputMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}

}
