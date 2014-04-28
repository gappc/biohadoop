package at.ac.uibk.dps.biohadoop.ga.master.kryo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

import com.esotericsoftware.kryonet.Connection;

public class GaKryoRunnable implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaKryoRunnable.class);

	private Message message;
	private Connection connection;
	private JobManager jobManager = JobManager.getInstance();
	
	public void setMessage(Message message) {
		this.message = message;
	}
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			Task currentTask = null;

			MessageType messageType = MessageType.NONE;
			Object response = null;

			if (message.getType() == MessageType.REGISTRATION_REQUEST) {
				messageType = MessageType.REGISTRATION_RESPONSE;
				response = null;
			}
			if (message.getType() == MessageType.WORK_INIT_REQUEST) {
				currentTask = (Task) jobManager
						.getTaskForExecution(Ga.GA_WORK_QUEUE);

				messageType = MessageType.WORK_INIT_RESPONSE;
				response = new Object[] {
						DistancesGlobal.getDistances(),
						currentTask };
			}
			if (message.getType() == MessageType.WORK_REQUEST) {
				GaResult result = (GaResult) message
						.getData();

//				System.out.println("1 " + result);

				jobManager.writeResult(
						Ga.GA_RESULT_STORE, result);
//				System.out.println("2 " + result);
				currentTask = (Task) jobManager
						.getTaskForExecution(Ga.GA_WORK_QUEUE);
//				System.out.println("3 " + result);
//				System.out.println("scheduling "
//						+ currentTask + "\n");

				if (currentTask instanceof StopTask) {
					messageType = MessageType.SHUTDOWN;
				} else {
					messageType = MessageType.WORK_RESPONSE;
				}
				response = currentTask;
			}

			connection.sendTCP(new Message(messageType,
					response));

			if (messageType == MessageType.SHUTDOWN) {
				LOGGER.info("KryoGaResource got SHUTDOWN message, now shutting down");
//				server.stop();
			}
		} catch (InterruptedException e) {
			LOGGER.error("Kryo Server error", e);
//			server.stop();
		}
	}

}
