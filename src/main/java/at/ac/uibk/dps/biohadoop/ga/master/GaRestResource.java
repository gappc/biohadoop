package at.ac.uibk.dps.biohadoop.ga.master;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;

@Path("/ga")
@Produces(MediaType.APPLICATION_JSON)
public class GaRestResource implements Endpoint {

	private static final Logger LOG = LoggerFactory.getLogger(GaRestResource.class);
	
	private Message<?> inputMessage;
	private Message<?> outputMessage;
	
	@GET
	@Path("registration")
	public Message<?> registration() throws InterruptedException {
		Master<?> master = new GaMasterImpl<>(this);
		master.handleRegistration();
		return outputMessage;
	}

	@GET
	@Path("workinit")
	public Message<?> workInit() throws InterruptedException {
		Master<?> master = new GaMasterImpl<>(this);
		master.handleWorkInit();
		return outputMessage;
	}

	@POST
	@Path("work")
	public Message<?> work(Message<?> message) throws InterruptedException {
		inputMessage = message;
		Master<?> master = new GaMasterImpl<>(this);
		try {
			master.handleWork();
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
}
