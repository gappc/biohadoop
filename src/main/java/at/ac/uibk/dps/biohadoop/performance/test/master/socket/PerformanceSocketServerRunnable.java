package at.ac.uibk.dps.biohadoop.performance.test.master.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;

public class PerformanceSocketServerRunnable implements WorkObserver, Runnable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PerformanceSocketServerRunnable.class);
	
	private JobManager jobManager = JobManager.getInstance();
	private boolean stop;

	@Override
	public void run() {
		jobManager.addObserver(this);
		try {
			int socketTimeout = 1000;
			ServerSocket serverSocket = new ServerSocket(30001);
			serverSocket.setSoTimeout(socketTimeout);
			
			Socket socket = null;
			int childThreadsCount = 0;
			String resourceName = PerformanceSocketResource.class.getSimpleName() + "-";
			while (!stop) {
				try {
					socket = serverSocket.accept();
					PerformanceSocketResource socketRunnable = new PerformanceSocketResource(socket);
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
	public void stop() {
		LOGGER.info("shutting down");
		stop = true;
	}
}
