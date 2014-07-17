package at.ac.uibk.dps.biohadoop.communication.master.socket2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public class SocketSuperServerConnection implements Runnable {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketSuperServerConnection.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<Future<Integer>> futures = new ArrayList<>();
	private final String queueName;
	private final Object registrationObject;

	private volatile boolean stop;

	public SocketSuperServerConnection(String queueName, Object registrationObject) {
		this.queueName = queueName;
		this.registrationObject = registrationObject;
	}

	@Override
	public void run() {
		try {
			String prefix = queueName;
			String host = HostInfo.getHostname();
			
			PortFinder.aquireBindingLock();
			int port = HostInfo.getPort(30000);
			ServerSocket serverSocket = new ServerSocket(port);
			PortFinder.releaseBindingLock();
			
			Environment.setPrefixed(prefix, Environment.SOCKET_HOST, host);
			Environment.setPrefixed(prefix, Environment.SOCKET_PORT,
					Integer.toString(port));

			LOG.info("host: " + HostInfo.getHostname() + "  port: " + port);

			int socketTimeout = 2000;
			serverSocket.setSoTimeout(socketTimeout);
			
			while (!stop) {
				try {
					Socket socket = serverSocket.accept();
					SocketSuperEndpoint socketRunnable = new SocketSuperEndpoint(socket,
							queueName, registrationObject);
					Future<Integer> future = executorService.submit(socketRunnable);
					futures.add(future);
				} catch (SocketTimeoutException e) {
					LOG.debug("Socket timeout after {} ms", socketTimeout, e);
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			LOG.error("ServerSocket error", e);
		}
	}

	public void stop() {
		LOG.info("Shutting down");
		stop = true;
		for (Future<Integer> future : futures) {
			future.cancel(true);
		}
		executorService.shutdown();
	}
}
