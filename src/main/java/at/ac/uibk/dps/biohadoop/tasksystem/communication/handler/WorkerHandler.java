package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Worker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerData;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;

public class WorkerHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerHandler.class);

	private final Map<TaskTypeId, WorkerData<?, ?, ?>> workerDatas = new HashMap<>();
	private Message lastMessage = null;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
		Task<?> inputTask = inputMessage.getTask();
		TaskTypeId taskTypeId = inputTask.getTaskTypeId();
		WorkerData<?, ?, ?> workerData = workerDatas.get(taskTypeId);

		if (inputMessage.getType() == MessageType.INITIAL_DATA_RESPONSE
				.ordinal()) {
			LOG.info("Getting initial data for TaskTypeId {}", taskTypeId);
			workerData = getWorkerData(inputTask);
			workerDatas.put(taskTypeId, workerData);
			inputMessage = lastMessage;
		}

		if (workerData == null) {
			Task<?> initialDataTask = new Task<>(inputTask.getTaskId(),
					taskTypeId, null);
			Message initialDataMessage = new Message(
					MessageType.INITIAL_DATA_REQUEST.ordinal(), initialDataTask);
			e.getChannel().write(initialDataMessage);
			lastMessage = inputMessage;
			return;
		}

		inputTask = inputMessage.getTask();
		taskTypeId = inputTask.getTaskTypeId();

		@SuppressWarnings("unchecked")
		Worker<Object, Object, Object> worker = (Worker<Object, Object, Object>) workerData
				.getWorker();
		Object initalData = workerData.getInitialData();
		Object data = inputTask.getData();

		Object result = worker.compute(data, initalData);

		Task<?> outputTask = new Task<>(inputTask.getTaskId(), taskTypeId,
				result);
		Message outputMessage = new Message(MessageType.WORK_REQUEST.ordinal(),
				outputTask);

		e.getChannel().write(outputMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}

	private <R, T, S> WorkerData<R, T, S> getWorkerData(Task<T> task)
			throws Exception {
		@SuppressWarnings("unchecked")
		TaskConfiguration<R> taskConfiguration = (TaskConfiguration<R>) task
				.getData();
		@SuppressWarnings("unchecked")
		Class<? extends Worker<R, T, S>> workerClass = (Class<? extends Worker<R, T, S>>) Class
				.forName(taskConfiguration.getWorkerClassName());
		Worker<R, T, S> worker = workerClass
				.newInstance();
		return new WorkerData<R, T, S>(worker,
				taskConfiguration.getInitialData());
	}
}
