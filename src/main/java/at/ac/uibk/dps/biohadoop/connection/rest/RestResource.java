package at.ac.uibk.dps.biohadoop.connection.rest;

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

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointHandler;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Produces(MediaType.APPLICATION_JSON)
public abstract class RestResource<T, S> implements Endpoint, MasterConnection,
		Master {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestResource.class);

	private DefaultEndpointHandler masterEndpoint;
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
	public Message<?> registration() throws InterruptedException {
		buildMasterEndpoint();
		masterEndpoint.handleRegistration();
		return outputMessage;
	}

	@GET
	@Path("workinit")
	public Message<T> workInit() throws InterruptedException {
		buildMasterEndpoint();
		masterEndpoint.handleWorkInit();
		return outputMessage;
	}

	@POST
	@Path("work")
	public Message<T> work(String messageString) throws InterruptedException {
		try {
			Message<S> message = new ObjectMapper().readValue(messageString,
					getInputType());
			buildMasterEndpoint();
			inputMessage = message;
			masterEndpoint.handleWork();
		} catch (IOException e) {
			LOG.error("Could not deserialize data {} to object {}",
					messageString, Message.class, e);
		}

		return outputMessage;
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
		masterEndpoint = DefaultEndpointHandler.newInstance(this,
				getQueueName(), getRegistrationObject());
	}

}
