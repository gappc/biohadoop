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
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueueService;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;

public class TestAdapterWorkHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(AdapterWorkHandler.class);

	private final TaskQueue taskQueue;

	public TestAdapterWorkHandler(String pipelineName) {
		taskQueue = TaskQueueService.getTaskQueue(pipelineName);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
//		e.getChannel().write(inputMessage);
		
		Task inputTask = inputMessage.getTask();
		Task outputTask = new Task(TaskId.newInstance(), new TaskTypeId(213l), new int[]{41, 13, 10, 14, 1, 26, 22, 46, 16, 25, 47, 23, 15, 11, 43, 27, 30, 7, 8, 45, 18, 2, 4, 40, 9, 0, 31, 37, 33, 36, 3, 39, 35, 28, 19, 20, 6, 44, 29, 32, 12, 42, 17, 24, 21, 34, 38, 5});
		Message outputMessage = new Message<>(
				MessageType.WORK_RESPONSE.ordinal(), outputTask);
		e.getChannel().write(outputMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}
}
