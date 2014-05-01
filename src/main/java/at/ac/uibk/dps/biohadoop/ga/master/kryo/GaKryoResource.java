package at.ac.uibk.dps.biohadoop.ga.master.kryo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.algorithm.GaResult;
import at.ac.uibk.dps.biohadoop.ga.algorithm.GaTask;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.StopTask;
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
	private Server server;
	private AtomicInteger workerCount = new AtomicInteger();
	
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private BlockingQueue<GaKryoRunnable> runnables = new LinkedBlockingQueue<GaKryoRunnable>();

	public GaKryoResource() {
		LOGGER.info("Starting Kryo server");
		jobManager.addObserver(this);

		Log.set(Log.LEVEL_DEBUG);
		try {
			server = new Server(64 * 1024, 64 * 1024);
			server.start();
			server.bind(30015);

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

			server.addListener(new Listener() {
				public void connected(Connection connection) {
					workerCount.incrementAndGet();
					runnables.add(new GaKryoRunnable());
					System.out.println("INC " + workerCount.get());
				}

				public void disconnected(Connection connection) {
					workerCount.decrementAndGet();
					System.out.println("DEC " + workerCount.get());
					stop();
				}

				public void received(final Connection connection,
						final Object object) {
					if (object instanceof Message) {
//						try {
//							GaKryoRunnable tmp = runnables.take();
							GaKryoRunnable tmp = new GaKryoRunnable();
							tmp.setConnection(connection);
							tmp.setMessage((Message)object);
							executorService.execute(tmp);
//							runnables.add(tmp);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
						
						
						
//						executorService.execute(new Runnable() {
//
//							@Override
//							public void run() {
//								try {
//									Task currentTask = null;
//									Message message = (Message) object;
//
//									MessageType messageType = MessageType.NONE;
//									Object response = null;
//
//									if (message.getType() == MessageType.REGISTRATION_REQUEST) {
//										messageType = MessageType.REGISTRATION_RESPONSE;
//										response = null;
//									}
//									if (message.getType() == MessageType.WORK_INIT_REQUEST) {
//										currentTask = (Task) jobManager
//												.getTaskForExecution(Ga.GA_WORK_QUEUE);
//
//										messageType = MessageType.WORK_INIT_RESPONSE;
//										response = new Object[] {
//												DistancesGlobal.getDistances(),
//												currentTask };
//									}
//									if (message.getType() == MessageType.WORK_REQUEST) {
//										GaResult result = (GaResult) message
//												.getData();
//
////										System.out.println("1 " + result);
//
//										jobManager.writeResult(
//												Ga.GA_RESULT_STORE, result);
////										System.out.println("2 " + result);
//										currentTask = (Task) jobManager
//												.getTaskForExecution(Ga.GA_WORK_QUEUE);
////										System.out.println("3 " + result);
////										System.out.println("scheduling "
////												+ currentTask + "\n");
//
//										if (currentTask instanceof StopTask) {
//											messageType = MessageType.SHUTDOWN;
//										} else {
//											messageType = MessageType.WORK_RESPONSE;
//										}
//										response = currentTask;
//									}
//
//									connection.sendTCP(new Message(messageType,
//											response));
//
//									if (messageType == MessageType.SHUTDOWN) {
//										LOGGER.info("KryoGaResource got SHUTDOWN message, now shutting down");
//										server.stop();
//									}
//								} catch (InterruptedException e) {
//									LOGGER.error("Kryo Server error", e);
//									server.stop();
//								}
//							}
//						});

					}
				}
			});
		} catch (Exception e) {
			LOGGER.error("Kryo Server BIIIIG error", e);
		}
	}

	@Override
	public void stop() {
		if (workerCount.intValue() == 0) {
			LOGGER.info("GaKryo server shutting down");
			executorService.shutdown();
			server.stop();
		}
	}
}
