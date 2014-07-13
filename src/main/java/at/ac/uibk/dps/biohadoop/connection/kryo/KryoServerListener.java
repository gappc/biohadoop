package at.ac.uibk.dps.biohadoop.connection.kryo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointImpl;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.CommunicationException;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.torename.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoServerListener extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoServerListener.class);

	private final Map<Connection, DefaultEndpointImpl> masters = new ConcurrentHashMap<>();
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
			DefaultEndpointImpl masterEndpoint = buildMaster(kryoEndpoint);
			masters.put(connection, masterEndpoint);
			zeroLock.increment();
		} catch (Exception e) {
			LOG.error("Could not start connection", e);
		}
	}

	public void disconnected(Connection connection) {
		zeroLock.decrement();
		DefaultEndpointImpl masterEndpoint = masters.get(connection);
		masters.remove(masterEndpoint);
		Task<?> task = masterEndpoint.getCurrentTask();
		if (task != null) {
			try {
				new TaskEndpointImpl<>(master.getQueueName()).reschedule(task
						.getTaskId());
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
					DefaultEndpointImpl endpointImpl = masters.get(connection);

					Message<?> inputMessage = (Message<?>) object;
					KryoEndpoint endpoint = (KryoEndpoint) endpointImpl
							.getEndpoint();
					endpoint.setConnection(connection);
					endpoint.setInputMessage(inputMessage);

					try {
						if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
							endpointImpl.handleRegistration();
						}
						if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
							endpointImpl.handleWorkInit();
						}
						if (inputMessage.getType() == MessageType.WORK_REQUEST) {
							endpointImpl.handleWork();
						}
					} catch (CommunicationException e) {
						LOG.error(
								"Error while communicating with worker, closing communication",
								e);
						return 1;
					}

					return 0;
				}
			});
		}
	}

	private DefaultEndpointImpl buildMaster(KryoEndpoint kryoEndpoint)
			throws Exception {
		return DefaultEndpointImpl.newInstance(kryoEndpoint,
				master.getQueueName(), master.getRegistrationObject());
	}
}
