package at.ac.uibk.dps.biohadoop.connection.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class SocketServer implements MasterConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketServer.class);

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(1);
	private SocketServerConnection socketServerConnection;
	
	protected MasterConfiguration masterConfiguration;

	@Override
	public void configure() {
		socketServerConnection = new SocketServerConnection(masterConfiguration);
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