package at.ac.uibk.dps.biohadoop.connection.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class SocketServerConnection implements Runnable, ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketServerConnection.class);

	private final MasterConfiguration masterConfiguration;

	private volatile boolean stop;

	public SocketServerConnection(MasterConfiguration masterConfiguration) {
		this.masterConfiguration = masterConfiguration;
	}

	@Override
	public void run() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
		try {
			String prefix = masterConfiguration.getPrefix();
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
			int childThreadsCount = 0;
			String threadName = "Socket-" + masterConfiguration.getPrefix() + "-";
			while (!stop) {
				try {
					socket = serverSocket.accept();
					SocketEndpoint socketRunnable = new SocketEndpoint(socket, masterConfiguration);
					Thread child = new Thread(socketRunnable, threadName
							+ childThreadsCount++);
					child.start();
				} catch (SocketTimeoutException e) {
					LOG.debug("Socket timeout after {} ms", socketTimeout);
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			LOG.error("ServerSocket error", e);
		}
	}

	@Override
	public void shutdown() {
		LOG.info("shutting down");
		stop = true;
	}
}
