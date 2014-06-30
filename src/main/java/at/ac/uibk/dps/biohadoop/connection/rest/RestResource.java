package at.ac.uibk.dps.biohadoop.connection.rest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointHandler;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;

@Produces(MediaType.APPLICATION_JSON)
public abstract class RestResource implements Endpoint, MasterConnection, Master {

	private static final Logger LOG = LoggerFactory.getLogger(RestResource.class);
	
	private DefaultEndpointHandler masterEndpoint;
	private Message<?> inputMessage;
	private Message<?> outputMessage;

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
	public Message<?> workInit() throws InterruptedException {
		buildMasterEndpoint();
		masterEndpoint.handleWorkInit();
		return outputMessage;
	}

	@POST
	@Path("work")
	public Message<?> work(Message<?> message) throws InterruptedException {
		buildMasterEndpoint();
		inputMessage = message;
		masterEndpoint.handleWork();
		return outputMessage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Message<T> receive() throws ReceiveException {
		return (Message<T>)inputMessage;
	}

	@Override
	public <T>void send(Message<T> message) throws SendException {
		outputMessage = message;
	}
	
	private void buildMasterEndpoint() {
		masterEndpoint = DefaultEndpointHandler.newInstance(this, getQueueName(), getRegistrationObject());
	}

}
