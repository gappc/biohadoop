package at.ac.uibk.dps.biohadoop.tasksystem.adapter.rest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.Dependent;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.HandleMessageException;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.TaskConsumer;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskId;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;
import at.ac.uibk.dps.biohadoop.webserver.handler.DeployingClasses;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
@Dependent
public class RestAdapter<R, T, S> implements Adapter {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestAdapter.class);
	private static final Map<String, TaskConsumer<?, ?, ?>> TASK_CONSUMERS = new ConcurrentHashMap<>();

	public void configure(String pipelineName) {
		TaskConsumer<?, ?, ?> taskConsumer = new TaskConsumer<>(pipelineName);
		TASK_CONSUMERS.put(pipelineName, taskConsumer);
		DeployingClasses.addRestfulClass(RestAdapter.class);
	}

	public void start() {
	}

	public void stop() {
	}

	@GET
	@Path("{path}/initialdata/{taskId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<T> getInitialData(@PathParam("path") String path,
			@PathParam("taskId") String taskIdString) {
		TaskId taskId = TaskId.newInstance(taskIdString);
		Message<S> inputMessage = (Message<S>) new Message<>(
				MessageType.REGISTRATION_REQUEST, new Task<>(taskId, null, null));

		Message<T> outputMessage = null;
		try {
			TaskConsumer<R, T, S> taskConsumer = getTaskConsumer(path);
			outputMessage = taskConsumer
					.handleMessage(inputMessage);
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request {}", inputMessage, e);
		}
		return outputMessage;
	}

	@GET
	@Path("{path}/workinit")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<T> workinit(@PathParam("path") String path) {
		TaskConsumer<R, T, S> taskConsumer = getTaskConsumer(path);
		if (taskConsumer == null) {
			return new Message<>(MessageType.ERROR, null);
		}
		Message<S> inputMessage = new Message<>(MessageType.WORK_INIT_REQUEST,
				null);

		Message<T> outputMessage = null;
		try {
			outputMessage = taskConsumer.handleMessage(inputMessage);
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request {}", inputMessage, e);
		}
		return outputMessage;
	}

	@POST
	@Path("{path}/work")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<T> work(@PathParam("path") String path, String message) {
		TaskConsumer<R, T, S> taskConsumer = getTaskConsumer(path);
		Message<T> outputMessage = null;
		try {
			Message<S> inputMessage = MessageConverter
					.getTypedMessageForMethod(message, "compute", -1);
			outputMessage = taskConsumer.handleMessage(inputMessage);
		} catch (ConversionException e) {
			LOG.error("Error while converting String to Message", e);
			rescheduleCurrentTask(taskConsumer, message);
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request {}", message, e);
			rescheduleCurrentTask(taskConsumer, message);
		}
		return outputMessage;
	}

	@SuppressWarnings("unchecked")
	private TaskConsumer<R, T, S> getTaskConsumer(String path) {
		return (TaskConsumer<R, T, S>) TASK_CONSUMERS.get(path);
	}

	private void rescheduleCurrentTask(TaskConsumer<?, ?, ?> taskConsumer,
			String message) {
		ObjectMapper objectMapper = new ObjectMapper();
		Task<?> task = null;
		try {
			Message<?> rescheduleMessage = objectMapper.readValue(message,
					Message.class);
			task = rescheduleMessage.getTask();

			if (task == null) {
				LOG.error("Could not reschedule null task");
				return;
			}
			taskConsumer.reschedule(task.getTaskId());
		} catch (IOException e) {
			LOG.error("Could not reschedule task", e);
		} catch (ShutdownException e) {
			LOG.error(
					"Error while rescheduling task {}, got ShutdownException",
					task.getTaskId(), e);
		}
	}

}
