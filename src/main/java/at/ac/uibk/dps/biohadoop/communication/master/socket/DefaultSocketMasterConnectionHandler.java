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

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedSocket;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public class DefaultSocketMasterConnectionHandler<R, T, S> implements Runnable {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultSocketMasterConnectionHandler.class);

	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final List<Future<Integer>> futures = new ArrayList<>();
	private Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass;
	private String path;
	
	private volatile boolean stop;

	public DefaultSocketMasterConnectionHandler(
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass) {
		this.remoteExecutableClass = remoteExecutableClass;
		path = DefaultTaskClient.QUEUE_NAME;
		if (remoteExecutableClass != null) {
			DedicatedSocket dedicated = remoteExecutableClass
					.getAnnotation(DedicatedSocket.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
	}

	@Override
	public void run() {
		try {
			String host = HostInfo.getHostname();
			
			PortFinder.aquireBindingLock();
			int port = HostInfo.getPort(30000);
			ServerSocket serverSocket = new ServerSocket(port);
			PortFinder.releaseBindingLock();
			
			Environment.setPrefixed(path, Environment.SOCKET_HOST, host);
			Environment.setPrefixed(path, Environment.SOCKET_PORT,
					Integer.toString(port));

			LOG.info("host: {} port: {} queue: {}", HostInfo.getHostname(), port, path);

			int socketTimeout = 2000;
			serverSocket.setSoTimeout(socketTimeout);
			
			while (!stop) {
				try {
					Socket socket = serverSocket.accept();
					DefaultSocketConnection<R, T, S> socketRunnable = new DefaultSocketConnection<>(socket, remoteExecutableClass, path);
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
