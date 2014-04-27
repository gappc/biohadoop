package at.ac.uibk.dps.biohadoop.ga.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.job.StopTask;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.job.WorkObserver;
import at.ac.uibk.dps.biohadoop.websocket.Message;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class GaKryoResource implements WorkObserver {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(GaKryoResource.class);

	private JobManager jobManager = JobManager.getInstance();
	private Task currentTask;
	private Server server;

	public GaKryoResource() {
		LOGGER.info("Starting Kryo server");
		jobManager.addObserver(this);

		Log.set(Log.LEVEL_DEBUG);
		try {
			server = new Server(64 * 1024, 64 * 1024);
			server.start();
			server.bind(30015);
			System.out.println("BIND DONE");
			
			Kryo kryo = server.getKryo();
			kryo.register(Message.class);
			kryo.register(MessageType.class);
			kryo.register(GaTask.class);
			kryo.register(GaResult.class);
			kryo.register(Object[].class);
			kryo.register(double[][].class);
			kryo.register(double[].class);
			kryo.register(int[].class);
			kryo.register(StopTask.class);
			System.out.println("REGISTRATION DONE");

			server.addListener(new Listener() {
				public void received(Connection connection, Object object) {
					if (object instanceof Message) {
						try {
							Message message = (Message) object;

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
								GaResult result = (GaResult) message.getData();
								jobManager.writeResult(Ga.GA_RESULT_STORE,
										result);
								currentTask = (Task) jobManager
										.getTaskForExecution(Ga.GA_WORK_QUEUE);

								if (currentTask instanceof StopTask) {
									messageType = MessageType.SHUTDOWN;
								} else {
									messageType = MessageType.WORK_RESPONSE;
								}
								response = currentTask;
							}

							connection.sendTCP(new Message(messageType,
									response));

							if (message.getType() == MessageType.SHUTDOWN) {
								LOGGER.info("KryoGaResource got SHUTDOWN message, now shutting down");
								server.stop();
							}
						} catch (InterruptedException e) {
							LOGGER.error("Kryo Server error", e);
							server.stop();
						}
					}
				}
			});
			System.out.println("ALL PREPARED");
		} catch (Exception e) {
			LOGGER.error("Kryo Server BIIIIG error", e);
		}
	}

	@Override
	public void stop() {
		LOGGER.info("GaKryo server shutting down");
		server.stop();
	}
}
