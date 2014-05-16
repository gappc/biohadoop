package at.ac.uibk.dps.biohadoop.moead.master.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.moead.algorithm.MoeadResult;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

public class MoeadSocketResource implements Runnable, WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MoeadSocketResource.class);
	private String className = MoeadSocketResource.class.getName();

	private Socket socket;
	private JobManager jobManager = JobManager.getInstance();
	private Task currentTask;

	public MoeadSocketResource(Socket socket) {
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

			int counter = 0;
			while (true) {
				counter++;
				if (counter % 10000 == 0) {
					counter = 0;
					os.reset();
				}
				
				Message message = (Message) is.readUnshared();

				messageType = MessageType.NONE;
				response = null;

				if (message.getType() == MessageType.REGISTRATION_REQUEST) {
					messageType = MessageType.REGISTRATION_RESPONSE;
					response = null;
				}
				if (message.getType() == MessageType.WORK_INIT_REQUEST) {
					currentTask = (Task) jobManager
							.getTaskForExecution(Moead.MOEAD_WORK_QUEUE);
					messageType = MessageType.WORK_INIT_RESPONSE;
					response = currentTask;
				}
				if (message.getType() == MessageType.WORK_REQUEST) {
					MoeadResult result = (MoeadResult)message.getData();
					jobManager.writeResult(Moead.MOEAD_RESULT_STORE, result);
					currentTask = (Task) jobManager
							.getTaskForExecution(Moead.MOEAD_WORK_QUEUE);

					if (currentTask instanceof StopTask) {
						messageType = MessageType.SHUTDOWN;
					} else {
						messageType = MessageType.WORK_RESPONSE;
					}
					response = currentTask;
				}

				os.writeUnshared(new Message(messageType, response));
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
						.reScheduleTask(Moead.MOEAD_WORK_QUEUE, currentTask);
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
