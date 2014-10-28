package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;
import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerData;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;

public class WorkerHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerHandler.class);
	
	private final Map<TaskTypeId, WorkerData> workerDatas = new HashMap<>();
	private Message lastMessage = null;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
		Task inputTask = inputMessage.getTask();
		TaskTypeId taskTypeId = inputTask.getTaskTypeId();
		WorkerData workerData = workerDatas.get(taskTypeId);
		
		if (inputMessage.getType() == MessageType.INITIAL_DATA_RESPONSE.ordinal()) {
			LOG.info("Getting initial data for TaskTypeId {}", taskTypeId);
			TaskConfiguration taskConfiguration = (TaskConfiguration)inputTask.getData();
			workerData = getWorkerData(inputTask);
			workerDatas.put(taskTypeId, workerData);
			inputMessage = lastMessage;
		}
		
		if (workerData == null) {
			Task initialDataTask = new Task(inputTask.getTaskId(), taskTypeId, null);
			Message initialDataMessage = new Message(
					MessageType.INITIAL_DATA_REQUEST.ordinal(), initialDataTask);
			e.getChannel().write(initialDataMessage);
			lastMessage = inputMessage;
			return;
		}
		
		inputTask = inputMessage.getTask();
		taskTypeId = inputTask.getTaskTypeId();
		
		AsyncComputable asyncComputable = workerData
				.getAsyncComputable();
		Object initalData = workerData.getInitialData();
		Object data = inputTask.getData();

		Object result = asyncComputable.compute(data, initalData);

		Task outputTask = new Task(inputTask.getTaskId(), taskTypeId, result);
		Message outputMessage = new Message<>(
				MessageType.WORK_REQUEST.ordinal(), outputTask);

		e.getChannel().write(outputMessage);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Handler error: ", e.getCause());
		e.getChannel().close();
	}
	
	private WorkerData getWorkerData(Task task) throws Exception {
		TaskConfiguration taskConfiguration = (TaskConfiguration)task.getData();
		Class<? extends AsyncComputable> asyncComputableClass = (Class<? extends AsyncComputable>) Class
				.forName(taskConfiguration.getAsyncComputableClassName());
		AsyncComputable asyncComputable = asyncComputableClass
				.newInstance();
		return new WorkerData(asyncComputable, taskConfiguration.getInitialData());
	}
}
