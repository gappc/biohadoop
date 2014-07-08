package at.ac.uibk.dps.biohadoop.connection.socket;

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

import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

public class SocketServerConnection implements Runnable {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketServerConnection.class);

	private final Master master;
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<Future<Integer>> futures = new ArrayList<>();

	private volatile boolean stop;

	public SocketServerConnection(Master master) {
		this.master = master;
	}

	@Override
	public void run() {
		try {
			String prefix = master.getQueueName();
			String host = HostInfo.getHostname();
			int port = HostInfo.getPort(30000);
			
			ServerSocket serverSocket = new ServerSocket(port);
			Environment.setPrefixed(prefix, Environment.SOCKET_HOST, host);
			Environment.setPrefixed(prefix, Environment.SOCKET_PORT,
					Integer.toString(port));

			LOG.info("host: " + HostInfo.getHostname() + "  port: " + port);

			int socketTimeout = 2000;
			serverSocket.setSoTimeout(socketTimeout);

			Socket socket = null;
			while (!stop) {
				try {
					socket = serverSocket.accept();
					SocketEndpoint socketRunnable = new SocketEndpoint(socket, master);
					Future<Integer> future = executorService.submit(socketRunnable);
					futures.add(future);
				} catch (SocketTimeoutException e) {
					LOG.debug("Socket timeout after {} ms", socketTimeout);
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
