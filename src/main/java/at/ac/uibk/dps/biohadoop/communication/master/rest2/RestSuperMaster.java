package at.ac.uibk.dps.biohadoop.communication.master.rest2;

import java.io.IOException;

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
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class RestSuperMaster implements MasterSendReceive, MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestSuperMaster.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	private DefaultMasterImpl masterEndpoint;
	private Message<?> inputMessage;
	private Message<?> outputMessage;

	@Override
	public void configure() {
		DeployingClasses.addWebSocketClass(this.getClass());
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
		try {
			buildMasterEndpoint(path);
			
			Class<? extends SuperComputable> superComputableClass = ResourcePath
					.getRestEntry(path);
			Class<?> receiveClass = superComputableClass.getAnnotation(RestMaster.class).receive();
			JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(Message.class, receiveClass);
			
			Message<?> message = OBJECT_MAPPER.readValue(messageString, javaType);
			inputMessage = message;
			masterEndpoint.handleWork();
			return outputMessage;
		} catch (IOException e) {
			LOG.error("Could not deserialize data {} to object {}",
					messageString, Message.class, e);
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
		Class<? extends SuperComputable> superComputable = ResourcePath
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
