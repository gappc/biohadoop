package at.ac.uibk.dps.biohadoop.connection.kryo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointHandler;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.torename.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoServerListener extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoServerListener.class);

	private final Map<Connection, DefaultEndpointHandler> masters = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();
	private final Master master;

	public KryoServerListener(Master master) {
		this.master = master;
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
			DefaultEndpointHandler masterEndpoint = buildMaster(kryoEndpoint);
			masters.put(connection, masterEndpoint);
			zeroLock.increment();
		} catch (Exception e) {
			LOG.error("Could not start connection", e);
		}
	}

	public void disconnected(Connection connection) {
		zeroLock.decrement();
		DefaultEndpointHandler masterEndpoint = masters.get(connection);
		masters.remove(masterEndpoint);
		Task<?> task = masterEndpoint.getCurrentTask();
		if (task != null) {
			try {
				new TaskEndpointImpl<>(master.getQueueName())
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
					DefaultEndpointHandler master = masters.get(connection);

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

	private DefaultEndpointHandler buildMaster(KryoEndpoint kryoEndpoint) throws Exception {
		return DefaultEndpointHandler.newInstance(kryoEndpoint, master.getQueueName(), master.getRegistrationObject());
	}
}
