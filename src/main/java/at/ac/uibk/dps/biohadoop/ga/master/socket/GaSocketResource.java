package at.ac.uibk.dps.biohadoop.ga.master.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

public class GaSocketResource implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaSocketResource.class);
	private String className = GaSocketResource.class.getName();

	private Socket socket;
	private JobManager jobManager = JobManager.getInstance();
	private Task currentTask;

	public GaSocketResource(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		jobManager.addObserver(this);
		try {
			LOGGER.info("{} opened Socket on server", className);

			ObjectOutputStream os = new ObjectOutputStream(
					new BufferedOutputStream(socket.getOutputStream()));
			os.flush();
			ObjectInputStream is = new ObjectInputStream(
					new BufferedInputStream(socket.getInputStream()));

			MessageType messageType = MessageType.NONE;
			Object response = null;

			while (true) {
				Message message = (Message) is.readObject();

				messageType = MessageType.NONE;
				response = null;

				if (message.getType() == MessageType.REGISTRATION_REQUEST) {
					messageType = MessageType.REGISTRATION_RESPONSE;
					response = null;
				}
				if (message.getType() == MessageType.WORK_INIT_REQUEST) {
					currentTask = (Task) jobManager
							.getTaskForExecution(Ga.GA_WORK_QUEUE);
					messageType = MessageType.WORK_INIT_RESPONSE;
					response = new Object[] {
							DistancesGlobal.getDistances(), currentTask };
				}
				if (message.getType() == MessageType.WORK_REQUEST) {
					GaResult result = (GaResult)message.getData();
					jobManager.writeResult(Ga.GA_RESULT_STORE, result);
					currentTask = (Task) jobManager
							.getTaskForExecution(Ga.GA_WORK_QUEUE);

					if (currentTask instanceof StopTask) {
						messageType = MessageType.SHUTDOWN;
					} else {
						messageType = MessageType.WORK_RESPONSE;
					}
					response = currentTask;
				}

				os.writeObject(new Message(messageType, response));
				os.flush();

				if (messageType == MessageType.SHUTDOWN) {
					break;
				}
			}
			os.close();
			is.close();
		} catch (Exception e) {
			LOGGER.error("Error while running {} socket server", className, e);
			try {
				jobManager
						.reScheduleTask(Ga.GA_WORK_QUEUE, currentTask);
			} catch (InterruptedException e1) {
				LOGGER.error("Could not reschedule task at {}", className, e);
			}
		}
	}
	
	@Override
	public void stop() {
		LOGGER.info("{} socket server shutting down", className);
	}
}
