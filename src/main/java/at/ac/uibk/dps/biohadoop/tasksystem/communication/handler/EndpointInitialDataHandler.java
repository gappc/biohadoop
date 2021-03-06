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
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskBroker;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskBrokerService;

public class EndpointInitialDataHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointInitialDataHandler.class);

	private final TaskBroker taskBroker = TaskBrokerService.getTaskBroker();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
		Task<?> inputTask = inputMessage.getTask();
		TaskConfiguration<?> taskConfiguration = taskBroker
				.getTaskConfiguration(inputTask.getTaskId());
		Task<?> outputTask = new Task<>(inputTask.getTaskId(),
				taskConfiguration.getTaskTypeId(), taskConfiguration);

		Message outputMessage = new Message(
				MessageType.INITIAL_DATA_RESPONSE.ordinal(), outputTask);

		e.getChannel().write(outputMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}
}
