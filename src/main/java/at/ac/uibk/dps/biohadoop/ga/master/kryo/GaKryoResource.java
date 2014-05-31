package at.ac.uibk.dps.biohadoop.ga.master.kryo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ShutdownException;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskId;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class GaKryoResource implements ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(GaKryoResource.class);

	private Server server;
	private AtomicInteger workerCount = new AtomicInteger();
	private ExecutorService executorService = Executors.newCachedThreadPool();
	private Map<Connection, GaMasterImpl<int[]>> masters = new ConcurrentHashMap<>();

	private CountDownLatch shutdownLatch = new CountDownLatch(1);
	private CountDownLatch workerLatch = new CountDownLatch(1);

	public GaKryoResource() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
		LOG.info("Starting Kryo server");

		Log.set(Log.LEVEL_DEBUG);
		try {
			server = new Server(64 * 1024, 64 * 1024);
			new Thread(server).start();
			server.bind(30015);

			Kryo kryo = server.getKryo();
			kryo.register(Message.class);
			kryo.register(MessageType.class);
			kryo.register(Object[].class);
			kryo.register(double[][].class);
			kryo.register(double[].class);
			kryo.register(int[].class);
			kryo.register(Task.class);
			kryo.register(TaskId.class);
			kryo.register(Double[][].class);
			kryo.register(Double[].class);

			server.addListener(new Listener() {
				public void connected(Connection connection) {
					workerCount.incrementAndGet();
					GaKryoEndpoint endpoint = new GaKryoEndpoint();
					endpoint.setConnection(connection);
					masters.put(connection, new GaMasterImpl<int[]>(
							new GaKryoEndpoint()));
					if (workerLatch.getCount() == 0) {
						workerLatch = new CountDownLatch(1);
					}
				}

				public void disconnected(Connection connection) {
					workerCount.decrementAndGet();
					if (workerCount.compareAndSet(0, 0)) {
						workerLatch.countDown();
					}
					Master<int[]> master = masters.get(connection);
					masters.remove(master);
					Task<int[]> task = master.getCurrentTask();
					if (task != null) {
						JobManager.<int[], Object> getInstance().reschedule(
								task, Ga.GA_QUEUE);
					}
				}

				public void received(final Connection connection,
						final Object object) {
					if (object instanceof Message) {

						executorService.submit(new Runnable() {

							@Override
							public void run() {
								GaMasterImpl<int[]> master = masters
										.get(connection);

								Message<?> inputMessage = (Message<?>) object;
								GaKryoEndpoint endpoint = ((GaKryoEndpoint) master
										.getEndpoint());
								endpoint.setConnection(connection);
								endpoint.setInputMessage(inputMessage);

								try {
									if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
										master.handleRegistration();
									}
									if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
										master.handleWorkInit();
									}
									if (inputMessage.getType() == MessageType.WORK_REQUEST) {
										master.handleWork();
									}
								} catch (ShutdownException e) {
									LOG.info("Got shutdown event");
									shutdownLatch.countDown();
								}
							}
						});
					}
				}
			});
		} catch (Exception e) {
			LOG.error("Kryo Server fatal error", e);
			workerCount.set(0);
			shutdownLatch.countDown();
			workerLatch.countDown();
		}
	}

	@Override
	public void shutdown() {
		LOG.info("KryoServer waiting to shut down");
		while (workerCount.intValue() != 0) {
			try {
				shutdownLatch.await();
				workerLatch.await();
			} catch (InterruptedException e) {
				LOG.error("Got Exception while waiting for latch", e);
			}
		}
		LOG.info("KryoServer shutting down");
		executorService.shutdown();
		server.stop();
	}

}
