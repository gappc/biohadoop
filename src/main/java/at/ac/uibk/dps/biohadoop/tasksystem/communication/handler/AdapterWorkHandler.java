package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueueService;

public class AdapterWorkHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(AdapterWorkHandler.class);

	private final TaskQueue taskQueue;
	ForkJoinPool pool = new ForkJoinPool();

	public AdapterWorkHandler(String pipelineName) {
		taskQueue = TaskQueueService.getTaskQueue(pipelineName);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message<?> inputMessage = (Message<?>) e.getMessage();
		if (inputMessage.getType() == MessageType.INITIAL_DATA_REQUEST
				.ordinal()) {
			ctx.sendUpstream(e);
		} else if (inputMessage.getType() == MessageType.WORK_REQUEST.ordinal()) {
			Task inputTask = inputMessage.getTask();
			if (inputTask != null) {
				taskQueue.storeResult(inputTask.getTaskId(),
						inputTask.getData());
			}

			// There may be no task available, therefore we try to get a task
			// using non-blocking poll. If the result is null, we use a blocking
			// approach inside a dedicated thread
			Task outputTask = taskQueue.pollTask();
			if (outputTask != null) {
				Message outputMessage = new Message(
						MessageType.WORK_RESPONSE.ordinal(), outputTask);
				e.getChannel().write(outputMessage);
			} else {
				TaskGetter taskGetter = new TaskGetter(e);
				pool.submit(taskGetter);
			}
		} else {
			throw new Exception("Invalid MessageType "
					+ MessageType.values()[inputMessage.getType()]);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}

	private class TaskGetter implements Callable<Object> {

		private final MessageEvent e;

		public TaskGetter(MessageEvent e) {
			this.e = e;
		}

		@Override
		public Object call() throws Exception {
			Task outputTask = taskQueue.getTask();
			Message outputMessage = new Message(
					MessageType.WORK_RESPONSE.ordinal(), outputTask);
			e.getChannel().write(outputMessage);
			return null;
		}

	}
}
