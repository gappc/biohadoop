package at.ac.uibk.dps.biohadoop.connection.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class SocketServer implements MasterConnection {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketServer.class);

	private final String className = SocketServer.class.getSimpleName();
	protected MasterConfiguration masterConfiguration;

	@Override
	public void configure() {
	}

	@Override
	public void start() {
		LOG.info("Starting {}", className);
		SocketServerConnection socketServerConnection = new SocketServerConnection(
				masterConfiguration);
		Thread thread = new Thread(socketServerConnection, className);
		thread.start();
	}

}