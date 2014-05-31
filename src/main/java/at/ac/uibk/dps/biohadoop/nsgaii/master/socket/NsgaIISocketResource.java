package at.ac.uibk.dps.biohadoop.nsgaii.master.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.nsgaii.master.NsgaIIMasterImpl;
import at.ac.uibk.dps.biohadoop.torename.Helper;

public class NsgaIISocketResource implements Runnable, Endpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(NsgaIISocketResource.class);
	private String className =  Helper.getClassname(NsgaIISocketResource.class);

	private Socket socket;
	int counter = 0;
	
	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	
	public NsgaIISocketResource(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		JobManager<double[], double[]> jobManager = JobManager.getInstance();
		Master<double[]> master = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			master = new NsgaIIMasterImpl<double[]>(this);
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
				Task<double[]> currentTask = master.getCurrentTask();
				if (currentTask != null) {
					boolean hasRescheduled = jobManager.reschedule(currentTask,
							NsgaII.NSGAII_QUEUE);
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
//		jobManager.addObserver(this);
//		try {
//			LOG.info("{} opened Socket on server", className);
//
//			ObjectOutputStream os = new ObjectOutputStream(
//					new BufferedOutputStream(socket.getOutputStream()));
//			os.flush();
//			ObjectInputStream is = new ObjectInputStream(
//					new BufferedInputStream(socket.getInputStream()));
//
//			MessageType messageType = MessageType.NONE;
//			Object response = null;
//
//			int counter = 0;
//			while (true) {
//				counter++;
//				if (counter % 10000 == 0) {
//					counter = 0;
//					os.reset();
//				}
//				
//				Message message = (Message) is.readUnshared();
//
//				messageType = MessageType.NONE;
//				response = null;
//
//				if (message.getType() == MessageType.REGISTRATION_REQUEST) {
//					messageType = MessageType.REGISTRATION_RESPONSE;
//					response = null;
//				}
//				if (message.getType() == MessageType.WORK_INIT_REQUEST) {
//					currentTask = (Task) jobManager
//							.getTaskForExecution(NsgaII.NSGAII_QUEUE);
//					messageType = MessageType.WORK_INIT_RESPONSE;
//					response = currentTask;
//				}
//				if (message.getType() == MessageType.WORK_REQUEST) {
//					MoeadResult result = (MoeadResult)message.getData();
//					jobManager.writeResult(NsgaII.NSGAII_QUEUE, result);
//					currentTask = (Task) jobManager
//							.getTaskForExecution(NsgaII.NSGAII_QUEUE);
//
//					if (currentTask instanceof StopTask) {
//						messageType = MessageType.SHUTDOWN;
//					} else {
//						messageType = MessageType.WORK_RESPONSE;
//					}
//					response = currentTask;
//				}
//
//				os.writeUnshared(new Message(messageType, response));
//				os.flush();
//				
//				if (messageType == MessageType.SHUTDOWN) {
//					break;
//				}
//			}
//			os.close();
//			is.close();
//		} catch (Exception e) {
//			LOG.error("Error while running {} socket server", className, e);
//			try {
//				jobManager
//						.reScheduleTask(NsgaII.NSGAII_QUEUE, currentTask);
//			} catch (InterruptedException e1) {
//				LOG.error("Could not reschedule task at {}", className, e);
//			}
//		}
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
