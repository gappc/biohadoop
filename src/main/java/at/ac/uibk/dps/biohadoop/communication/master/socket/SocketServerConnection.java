package at.ac.uibk.dps.biohadoop.communication.master.socket;

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
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

public class SocketServerConnection implements Runnable {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketServerConnection.class);

	private final MasterEndpoint master;
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<Future<Integer>> futures = new ArrayList<>();

	private volatile boolean stop;

	public SocketServerConnection(MasterEndpoint master) {
		this.master = master;
	}

	@Override
	public void run() {
		try {
			String prefix = master.getQueueName();
			String host = HostInfo.getHostname();
			int port = HostInfo.getPort(30000);

			Environment.setPrefixed(prefix, Environment.SOCKET_HOST, host);
			Environment.setPrefixed(prefix, Environment.SOCKET_PORT,
					Integer.toString(port));

			LOG.info("host: " + HostInfo.getHostname() + "  port: " + port);

			runSocket(port);
		} catch (IOException e) {
			LOG.error("ServerSocket error", e);
		}
	}

	private void runSocket(int port)
			throws IOException {
		ServerSocket serverSocket = new ServerSocket(port);
		int socketTimeout = 2000;
		serverSocket.setSoTimeout(socketTimeout);
		
		while (!stop) {
			try {
				Socket socket = serverSocket.accept();
				SocketEndpoint socketRunnable = new SocketEndpoint(socket,
						master);
				Future<Integer> future = executorService.submit(socketRunnable);
				futures.add(future);
			} catch (SocketTimeoutException e) {
				LOG.debug("Socket timeout after {} ms", socketTimeout, e);
			}
		}
		serverSocket.close();
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