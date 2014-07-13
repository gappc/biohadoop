package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationException;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.MasterSendReceive;
import at.ac.uibk.dps.biohadoop.communication.master.ReceiveException;
import at.ac.uibk.dps.biohadoop.communication.master.SendException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Produces(MediaType.APPLICATION_JSON)
public abstract class RestResource<T, S> implements MasterSendReceive, MasterLifecycle,
		MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestResource.class);

	private DefaultMasterImpl masterEndpoint;
	private Message<S> inputMessage;
	private Message<T> outputMessage;

	public abstract TypeReference<Message<S>> getInputType();

	@Override
	public void configure() {
		DeployingClasses.addRestfulClass(this.getClass());
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@PostConstruct
	public void init() {
		ShutdownWaitingService.register();
	}

	@PreDestroy
	public void finish() {
		ShutdownWaitingService.unregister();
	}

	@GET
	@Path("registration")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> registration() {
		buildMasterEndpoint();
		try {
			masterEndpoint.handleRegistration();
			return outputMessage;
		} catch (CommunicationException e) {
			LOG.error("Error while communicating with worker, closing communication", e);
		}
		return null;
	}

	@GET
	@Path("workinit")
	public Message<T> workInit() {
		buildMasterEndpoint();
		try {
			masterEndpoint.handleWorkInit();
			return outputMessage;			
		} catch (CommunicationException e) {
			LOG.error("Error while communicating with worker, closing communication", e);
		}
		return null;
	}

	@POST
	@Path("work")
	public Message<T> work(String messageString) {
		try {
			Message<S> message = new ObjectMapper().readValue(messageString,
					getInputType());
			buildMasterEndpoint();
			inputMessage = message;
			masterEndpoint.handleWork();
			return outputMessage;
		} catch (IOException e) {
			LOG.error("Could not deserialize data {} to object {}",
					messageString, Message.class, e);
		} catch (CommunicationException e) {
			LOG.error("Error while communicating with worker, closing communication", e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Message<U> receive() throws ReceiveException {
		return (Message<U>) inputMessage;
	}

	@Override
	public <U> void send(Message<U> message) throws SendException {
		outputMessage = (Message<T>) message;
	}

	private void buildMasterEndpoint() {
		masterEndpoint = DefaultMasterImpl.newInstance(this,
				getQueueName(), getRegistrationObject());
	}

}
