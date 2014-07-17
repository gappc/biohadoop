package at.ac.uibk.dps.biohadoop.communication.master.socket2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;

public class SocketSuperServer implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketSuperServer.class);

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(1);
	private final String queueName;
	private final Object registrationObject;
	
	private SocketSuperServerConnection socketServerConnection;
	
	public SocketSuperServer(String queueName, Object registrationObject) {
		this.queueName = queueName;
		this.registrationObject = registrationObject;
	}

	@Override
	public void configure() {
		socketServerConnection = new SocketSuperServerConnection(queueName, registrationObject);
	}

	@Override
	public void start() {
		LOG.info("Starting Socket server");
		executorService.submit(socketServerConnection);
	}

	@Override
	public void stop() {
		LOG.info("Socket Server waiting to shut down");
		socketServerConnection.stop();
		executorService.shutdown();
		LOG.info("Socket Server successful shut down");
	}

}