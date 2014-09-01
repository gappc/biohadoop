package at.ac.uibk.dps.biohadoop.communication.master.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

public class DefaultSocketEndpoint implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultSocketEndpoint.class);

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(1);
	
	private DefaultSocketMasterConnectionHandler<?, ?, ?> socketServerConnection;

	@Override
	public void configure(String queueName) {
		socketServerConnection = new DefaultSocketMasterConnectionHandler<>(queueName);
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