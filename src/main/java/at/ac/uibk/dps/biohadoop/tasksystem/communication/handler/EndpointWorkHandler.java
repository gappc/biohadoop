package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskBroker;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskBrokerService;

public class EndpointWorkHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointWorkHandler.class);

	private final TaskBroker taskBroker = TaskBrokerService.getTaskBroker();
	private final ForkJoinPool pool = new ForkJoinPool();

	private TaskId currentTaskId;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
		if (inputMessage.getType() == MessageType.INITIAL_DATA_REQUEST
				.ordinal()) {
			ctx.sendUpstream(e);
		} else if (inputMessage.getType() == MessageType.WORK_REQUEST.ordinal()) {
			Task<?> inputTask = inputMessage.getTask();
			if (inputTask != null) {
				taskBroker.storeResult(inputTask.getTaskId(),
						inputTask.getData());
			}

			// There may be no task available, therefore we try to get a task
			// using non-blocking poll. If the result is null, we use a blocking
			// approach inside a dedicated thread
			Task<?> outputTask = taskBroker.pollTask();
			if (outputTask != null) {
				currentTaskId = outputTask.getTaskId();
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
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		handleError();
		super.channelDisconnected(ctx, e);
	}

	public void handleError() {
		if (Environment.isShutdown()) {
			LOG.debug("No rescheduling done, as Biohadoop is already shutting down");
			return;
		}
		if (currentTaskId != null) {
			try {
				taskBroker.reschedule(currentTaskId);
			} catch (TaskException | InterruptedException e) {
				LOG.error("Error while rescheduling task {}", currentTaskId);
			}
			currentTaskId = null;
		}
		else {
			LOG.warn("TaskId is null, maybe the exception was raised before the first task was taken from queue?");
		}
	}

	private class TaskGetter implements Callable<Object> {

		private final MessageEvent e;

		public TaskGetter(MessageEvent e) {
			this.e = e;
		}

		@Override
		public Object call() throws Exception {
			Task<?> outputTask = taskBroker.getTask();
			currentTaskId = outputTask.getTaskId();
			Message outputMessage = new Message(
					MessageType.WORK_RESPONSE.ordinal(), outputTask);
			e.getChannel().write(outputMessage);
			return null;
		}

	}
}
