package at.ac.uibk.dps.biohadoop.nsgaii.master.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;

public class NsgaIISocketServerRunnable implements ShutdownHandler, Runnable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NsgaIISocketServerRunnable.class);
	
	private boolean stop;

	@Override
	public void run() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
		try {
			int socketTimeout = 1000;
			ServerSocket serverSocket = new ServerSocket(30001);
			serverSocket.setSoTimeout(socketTimeout);
			
			Socket socket = null;
			int childThreadsCount = 0;
			String resourceName = NsgaIISocketResource.class.getSimpleName() + "-";
			while (!stop) {
				try {
					socket = serverSocket.accept();
					NsgaIISocketResource socketRunnable = new NsgaIISocketResource(socket);
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
