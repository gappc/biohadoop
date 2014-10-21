package at.ac.uibk.dps.biohadoop.tasksystem.adapter.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;

public class SocketAdapter implements Adapter {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketAdapter.class);

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(1);
	
	private SocketConnectionHandler<?, ?, ?> socketServerConnection;

//	@Override
//	public void configure(String pipelineName) {
//		socketServerConnection = new SocketConnectionHandler<>(pipelineName);
//	}
	
	@Override
	public void start(String pipelineName) {
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

	@Override
	public int getPort(String pipelineName) throws AdapterException {
		// TODO Auto-generated method stub
		return 0;
	}

}