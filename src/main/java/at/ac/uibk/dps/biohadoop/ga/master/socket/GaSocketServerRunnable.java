package at.ac.uibk.dps.biohadoop.ga.master.socket;

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

public class GaSocketServerRunnable implements Runnable, ShutdownHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaSocketServerRunnable.class);
	
	private boolean stop;

	@Override
	public void run() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
		try {
			int port = HostInfo.getPort(30001);
			ServerSocket serverSocket = new ServerSocket(port);
			Environment.set(Environment.SOCKET_HOST, HostInfo.getHostname());
			Environment.set(Environment.SOCKET_PORT, Integer.toString(port));
			
			int socketTimeout = 2000;
			serverSocket.setSoTimeout(socketTimeout);
			
			Socket socket = null;
			int childThreadsCount = 0;
			String resourceName = GaSocketResource.class.getSimpleName() + "-";
			while (!stop) {
				try {
					socket = serverSocket.accept();
					GaSocketResource socketRunnable = new GaSocketResource(socket);
					Thread child = new Thread(socketRunnable, resourceName + childThreadsCount++);
					child.start();
				} catch(SocketTimeoutException e) {
					LOGGER.debug("Socket timeout after {} ms", socketTimeout);
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			LOGGER.error("ServerSocket error", e);
		}
	}

	@Override
	public void shutdown() {
		LOGGER.info("shutting down");
		stop = true;
	}
}
