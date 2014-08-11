package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapper;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapperUtils;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ResourcePath;
import at.ac.uibk.dps.biohadoop.utils.convert.ConversionException;
import at.ac.uibk.dps.biohadoop.utils.convert.MessageConverter;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class DefaultRestEndpoint implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestMasterEndpoint.class);
	private static final Map<String, DefaultMasterImpl> MASTERS = new ConcurrentHashMap<>();

	public void configure(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass) {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (remoteExecutableClass != null) {
			DedicatedRest dedicated = remoteExecutableClass
					.getAnnotation(DedicatedRest.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
				ResourcePath.addRestEntry(path, remoteExecutableClass);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		MASTERS.put(path, DefaultMasterImpl.newInstance(path));
		DeployingClasses.addRestfulClass(DefaultRestEndpoint.class);
	}

	public void start() {
	}

	public void stop() {
	}

	@GET
	@Path("{path}/initialdata/{classname}")
	@Produces(MediaType.APPLICATION_JSON)
	public <T> Message<ClassNameWrapper<T>> registration(
			@PathParam("path") String path,
			@PathParam("classname") String className) {
		try {
			DefaultMasterImpl masterEndpoint = MASTERS.get(path);
			Object registrationObject = getInitialData(className);

			Message<T> registrationMessage = (Message<T>) masterEndpoint
					.handleRegistration(registrationObject);
			return ClassNameWrapperUtils.wrapMessage(registrationMessage,
					className);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("{path}/workinit")
	@Produces(MediaType.APPLICATION_JSON)
	public <T> Message<ClassNameWrapper<T>> workinit(
			@PathParam("path") String path) {
		DefaultMasterImpl masterEndpoint = MASTERS.get(path);
		return (Message<ClassNameWrapper<T>>) masterEndpoint.handleWorkInit();
	}

	@POST
	@Path("{path}/work")
	@Produces(MediaType.APPLICATION_JSON)
	public <T> Message<ClassNameWrapper<T>> work(
			@PathParam("path") String path, String message) {
		DefaultMasterImpl masterEndpoint = MASTERS.get(path);
		try {
			Message<ClassNameWrapper<T>> inputMessage = MessageConverter
					.getMessageForMethod(message, "compute", -1);
			Message<ClassNameWrapper<T>> outputMessage = (Message<ClassNameWrapper<T>>) masterEndpoint
					.handleWork(inputMessage);
			return outputMessage;
		} catch (ConversionException e) {
			LOG.error("Error while converting String to Message", e);
			tryReschedule(path, message);
		}
		return null;
	}

	private Object getInitialData(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
				.forName(className);
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		return master.getInitalData();
	}

	private void tryReschedule(String queueName, String message) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Message<?> rescheduleMessage = objectMapper.readValue(message,
					Message.class);

			TaskEndpoint<?, ?> taskEndpoint = new TaskEndpointImpl<>(queueName);
			taskEndpoint.reschedule(rescheduleMessage.getTask().getTaskId());
		} catch (IOException | InterruptedException e) {
			LOG.error("Could not reschedule task", e);
		}
	}

}
