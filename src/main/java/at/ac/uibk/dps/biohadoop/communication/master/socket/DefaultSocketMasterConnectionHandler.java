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

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public class DefaultSocketMasterConnectionHandler<R, T, S> implements Runnable {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultSocketMasterConnectionHandler.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<Future<Object>> futures = new ArrayList<>();
	private String settingName;
	
	private volatile boolean stop;

	public DefaultSocketMasterConnectionHandler(String settingName) {
		this.settingName = settingName;
	}

	@Override
	public void run() {
		try {
			String host = HostInfo.getHostname();
			
			PortFinder.aquireBindingLock();
			int port = HostInfo.getPort(30000);
			ServerSocket serverSocket = new ServerSocket(port);
			PortFinder.releaseBindingLock();
			
			Environment.setPrefixed(settingName, Environment.SOCKET_HOST, host);
			Environment.setPrefixed(settingName, Environment.SOCKET_PORT,
					Integer.toString(port));

			LOG.info("host: {} port: {} setting: {}", HostInfo.getHostname(), port, settingName);

			int socketTimeout = 2000;
			serverSocket.setSoTimeout(socketTimeout);
			
			while (!stop) {
				try {
					Socket socket = serverSocket.accept();
					DefaultSocketConnection<R, T, S> socketRunnable = new DefaultSocketConnection<>(socket, settingName);
					Future<Object> future = executorService.submit(socketRunnable);
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
		for (Future<Object> future : futures) {
			future.cancel(true);
		}
		executorService.shutdown();
	}
}
