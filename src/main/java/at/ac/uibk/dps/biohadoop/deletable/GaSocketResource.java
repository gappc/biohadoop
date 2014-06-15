package at.ac.uibk.dps.biohadoop.deletable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.torename.Helper;

public class GaSocketResource implements Runnable, Endpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaSocketResource.class);
	private String className = Helper.getClassname(GaSocketResource.class);

	private Socket socket;
	int counter = 0;
	
	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;

	public GaSocketResource(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		JobManager<int[], Double> jobManager = JobManager.getInstance();
		MasterEndpoint master = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			master = new GaMasterImpl(this);
			master.handleRegistration();
			master.handleWorkInit();
			while (true) {
				master.handleWork();
			}
		} catch (ShutdownException e) {
			LOG.info("Got shutdown event");
		} catch (Exception e) {
			LOG.error("Error while running {}", className, e);
			if (master != null) {
				Task currentTask = master.getCurrentTask();
				if (currentTask != null) {
					boolean hasRescheduled = jobManager.reschedule(currentTask,
							Ga.GA_QUEUE);
					if (!hasRescheduled) {
						LOG.error("Could not reschedule task at {}", currentTask);
					}
				}
			}
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					LOG.error("Error while closing OutputStream", e);
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("Error while closing InputStream", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Message<T> receive() throws ReceiveException {
		try {
			return (Message<T>) is.readUnshared();
		} catch (ClassNotFoundException | IOException e) {
			LOG.error("Error while receiving", e);
			throw new ReceiveException(e);
		}
	}

	public void send(Message<?> message) throws SendException {
		try {
			counter++;
			if (counter % 10000 == 0) {
				counter = 0;
				os.reset();
			}
			os.writeUnshared(message);
			os.flush();
		} catch (IOException e) {
			LOG.error("Error while sending", e);
			throw new SendException(e);
		}
	}
}
