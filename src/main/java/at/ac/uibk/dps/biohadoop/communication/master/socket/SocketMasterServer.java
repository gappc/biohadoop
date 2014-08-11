package at.ac.uibk.dps.biohadoop.communication.master.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

public class SocketMasterServer implements MasterEndpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketMasterServer.class);

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(1);
	
	private SocketMasterServerConnection socketServerConnection;

	@Override
	public void configure(Class<? extends RemoteExecutable<?, ?, ?>> master) {
		socketServerConnection = new SocketMasterServerConnection(master);
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