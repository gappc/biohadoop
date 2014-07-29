package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationException;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.MasterSendReceive;
import at.ac.uibk.dps.biohadoop.communication.master.ReceiveException;
import at.ac.uibk.dps.biohadoop.communication.master.SendException;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketMaster;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class RestMasterEndpoint implements MasterSendReceive, MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestMasterEndpoint.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private DefaultMasterImpl masterEndpoint;
	private Message<?> inputMessage;
	private Message<?> outputMessage;

	@Override
	public void configure(Class<? extends Master> master) {
		Annotation annotation = master.getAnnotation(RestMaster.class);
		ResourcePath.addRestEntry(
				((RestMaster) annotation).path(),
				master);
		DeployingClasses.addRestfulClass(RestMasterEndpoint.class);
	}

	@Override
	public void start() {
	}
	
	@Override
	public void stop() {
	}
	
	@GET
	@Path("{path}/registration")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> registration(@PathParam("path") String path) {
		try {
			buildMasterEndpoint(path);
			masterEndpoint.handleRegistration();
			return outputMessage;
		} catch (CommunicationException e) {
			LOG.error(
					"Error while communicating with worker, closing communication",
					e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@GET
	@Path("{path}/workinit")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> workinit(@PathParam("path") String path) {
		try {
			buildMasterEndpoint(path);
			masterEndpoint.handleWorkInit();
			return outputMessage;
		} catch (CommunicationException e) {
			LOG.error(
					"Error while communicating with worker, closing communication",
					e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("{path}/work")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> work(@PathParam("path") String path, String messageString) {
		Class<?> receiveClass = null;
		try {
			buildMasterEndpoint(path);
			
			Class<? extends Master> superComputableClass = ResourcePath
					.getRestEntry(path);
			receiveClass = superComputableClass.getAnnotation(RestMaster.class).receive();
			JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(Message.class, receiveClass);
			
			Message<?> message = OBJECT_MAPPER.readValue(messageString, javaType);
			inputMessage = message;
			masterEndpoint.handleWork();
			return outputMessage;
		} catch (IOException e) {
			LOG.error("Could not deserialize data {} to object {}",
					messageString, receiveClass, e);
		} catch (CommunicationException e) {
			LOG.error(
					"Error while communicating with worker, closing communication",
					e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private void buildMasterEndpoint(String path) throws InstantiationException, IllegalAccessException {
		Class<? extends Master> superComputable = ResourcePath
				.getRestEntry(path);
		String queueName = superComputable.getAnnotation(RestMaster.class)
				.queueName();
		Object registrationObject = superComputable.newInstance()
				.getRegistrationObject();
		masterEndpoint = DefaultMasterImpl.newInstance(this, queueName,
				registrationObject);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Message<T> receive() throws ReceiveException {
		return (Message<T>) inputMessage;
	}

	@Override
	public <T> void send(Message<T> message) throws SendException {
		outputMessage = message;
	}
	
}
