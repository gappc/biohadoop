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
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerData;

public class TestWorkerHandler extends SimpleChannelHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(TestWorkerHandler.class);
	
	private final Map<TaskTypeId, WorkerData> workerDatas = new HashMap<>();
	private Message lastMessage = null;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		Message inputMessage = (Message) e.getMessage();
//		e.getChannel().write(inputMessage);
		
		Task inputTask = inputMessage.getTask();
		Task outputTask = new Task(inputTask.getTaskId(), inputTask.getTaskTypeId(), new Double(50000));
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
