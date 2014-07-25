package at.ac.uibk.dps.biohadoop.communication.master.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.Master;

public class SocketMasterServer implements MasterLifecycle {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketMasterServer.class);

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(1);
	private final Class<? extends Master> masterClass;
	
	private SocketMasterServerConnection socketServerConnection;
	
	public SocketMasterServer(Class<? extends Master> masterClass) {
		this.masterClass = masterClass;
	}

	@Override
	public void configure() {
		socketServerConnection = new SocketMasterServerConnection(masterClass);
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