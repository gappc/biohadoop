package at.ac.uibk.dps.biohadoop.connection.kryo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.torename.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoServerListener extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoServerListener.class);

	private final Map<Connection, MasterEndpoint> masters = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();
	private final Class<? extends MasterEndpoint> masterEndpointClass;

	public KryoServerListener(
			Class<? extends MasterEndpoint> masterEndpointClass) {
		this.masterEndpointClass = masterEndpointClass;
	}

	public void stop() {
		try {
			zeroLock.await();
			executorService.shutdown();
			LOG.info("KryoServer successful shut down");
		} catch (InterruptedException e) {
			LOG.error("Got Exception while waiting for latch", e);
		}
	}

	public void connected(Connection connection) {
		KryoEndpoint kryoEndpoint = new KryoEndpoint();
		kryoEndpoint.setConnection(connection);

		try {
			MasterEndpoint masterEndpoint = buildMaster(masterEndpointClass,
					kryoEndpoint);
			masters.put(connection, masterEndpoint);
			zeroLock.increment();
		} catch (Exception e) {
			LOG.error("Could not start connection", e);
		}
	}

	public void disconnected(Connection connection) {
		zeroLock.decrement();
		MasterEndpoint master = masters.get(connection);
		masters.remove(master);
		Task<?> task = master.getCurrentTask();
		if (task != null) {
			try {
				new TaskEndpointImpl<>(Ga.GA_QUEUE)
						.reschedule(task.getTaskId());
			} catch (InterruptedException e) {
				LOG.error("Could not reschedule task at {}", task);
			}
		}
	}

	public void received(final Connection connection, final Object object) {
		if (object instanceof Message) {

			executorService.submit(new Callable<Integer>() {

				@Override
				public Integer call() {
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

					return 0;
				}
			});
		}
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
