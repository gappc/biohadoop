package at.ac.uibk.dps.biohadoop.connection.kryo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.connection.MasterConnection;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.TaskId;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class KryoServer implements ShutdownHandler, MasterConnection {

	private static final Logger LOG = LoggerFactory.getLogger(KryoServer.class);

	private final AtomicInteger workerCount = new AtomicInteger();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final Map<Connection, MasterEndpoint> masters = new ConcurrentHashMap<>();
	private final CountDownLatch shutdownLatch = new CountDownLatch(1);

	private Server server;
	private CountDownLatch workerLatch = new CountDownLatch(1);

	protected MasterConfiguration masterConfiguration;

	@Override
	public void configure() {
	}

	@Override
	public void start() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
		LOG.info("Starting Kryo server");

		Log.set(Log.LEVEL_DEBUG);
		try {
			server = new Server(64 * 1024, 64 * 1024);
			new Thread(server).start();

			String prefix = masterConfiguration.getPrefix();
			String host = HostInfo.getHostname();
			int port = HostInfo.getPort(30000);

			server.bind(port);

			Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_HOST, host);
			Environment.setPrefixed(prefix, Environment.KRYO_SOCKET_PORT,
					Integer.toString(port));

			LOG.info("host: " + HostInfo.getHostname() + "  port: " + port);

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
					KryoEndpoint kryoEndpoint = new KryoEndpoint();
					kryoEndpoint.setConnection(connection);

					MasterEndpoint masterEndpoint;
					try {
						masterEndpoint = buildMaster(
								masterConfiguration.getMasterEndpoint(),
								kryoEndpoint);
						masters.put(connection, masterEndpoint);
						if (workerLatch.getCount() == 0) {
							workerLatch = new CountDownLatch(1);
						}
					} catch (Exception e) {
						LOG.error("Could not start connection", e);
					}
				}

				public void disconnected(Connection connection) {
					workerCount.decrementAndGet();
					if (workerCount.compareAndSet(0, 0)) {
						workerLatch.countDown();
					}
					MasterEndpoint master = masters.get(connection);
					masters.remove(master);
					Task task = master.getCurrentTask();
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
								MasterEndpoint master = masters.get(connection);

								Message<?> inputMessage = (Message<?>) object;
								KryoEndpoint endpoint = ((KryoEndpoint) master
										.getEndpoint());
								endpoint.setConnection(connection);
								endpoint.setInputMessage(inputMessage);

								if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
									master.handleRegistration();
								}
								if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
									master.handleWorkInit();
								}
								if (inputMessage.getType() == MessageType.WORK_REQUEST) {
									master.handleWork();
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
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (workerCount.intValue() != 0) {
					try {
						workerLatch.await(100, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						LOG.error("Got Exception while waiting for latch", e);
					}
				}
				LOG.info("KryoServer shutting down");
				executorService.shutdown();
				server.stop();
			}
		}).start();
	}

	private MasterEndpoint buildMaster(
			Class<? extends MasterEndpoint> masterEndpointClass,
			KryoEndpoint kryoEndpoint) throws Exception {
		try {
			Constructor<? extends MasterEndpoint> constructor = masterEndpointClass
					.getDeclaredConstructor(Endpoint.class);
			return constructor.newInstance(kryoEndpoint);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			LOG.error("Could not instanciate new {} with parameter {}",
					masterEndpointClass, kryoEndpoint);
			throw new Exception(e);
		}
	}

}
