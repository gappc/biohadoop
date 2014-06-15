package at.ac.uibk.dps.biohadoop.connection.rest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

@Produces(MediaType.APPLICATION_JSON)
public class RestResource implements Endpoint, MasterConnection {

	private static final Logger LOG = LoggerFactory.getLogger(RestResource.class);
	
	protected MasterConfiguration masterConfiguration;
	private MasterEndpoint masterEndpoint;
	
	private Message<?> inputMessage;
	private Message<?> outputMessage;

	@Override
	public void configure() {
		DeployingClasses.addRestfulClass(this.getClass());
	}

	@Override
	public void start() {
	}
	
	@GET
	@Path("registration")
	@Produces(MediaType.APPLICATION_JSON)
	public Message<?> registration() throws InterruptedException {
		setMasterEndpoint();
		masterEndpoint.handleRegistration();
		return outputMessage;
	}

	@GET
	@Path("workinit")
	public Message<?> workInit() throws InterruptedException {
		setMasterEndpoint();
		masterEndpoint.handleWorkInit();
		return outputMessage;
	}

	@POST
	@Path("work")
	public Message<?> work(Message<?> message) throws InterruptedException {
		setMasterEndpoint();
		inputMessage = message;
		try {
			masterEndpoint.handleWork();
		} catch (ShutdownException e) {
			LOG.info("Got shutdown event");
		}
		return outputMessage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Message<T> receive() throws ReceiveException {
		return (Message<T>)inputMessage;
	}

	@Override
	public void send(Message<?> message) throws SendException {
		outputMessage = message;
	}
	
	private void setMasterEndpoint() {
		Class<? extends MasterEndpoint> masterEnpointClass = masterConfiguration.getMasterEndpoint();
		try {
			Constructor<? extends MasterEndpoint> constructor = masterEnpointClass.getDeclaredConstructor(Endpoint.class);
			masterEndpoint = constructor.newInstance(this);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOG.error("Could not instanciate new {} with parameter {}", masterEnpointClass, this);
		}
	}

}
