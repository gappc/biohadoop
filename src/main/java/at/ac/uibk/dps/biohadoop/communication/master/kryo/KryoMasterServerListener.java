package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationException;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.socket.SocketMaster;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.utils.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoMasterServerListener extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoMasterServerListener.class);

	private final Map<Connection, DefaultMasterImpl> masters = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();
	private final Class<? extends Master> masterClass;
	private final String queueName;

	public KryoMasterServerListener(Class<? extends Master> masterClass) {
		this.masterClass = masterClass;
		queueName = masterClass.getAnnotation(KryoMaster.class).queueName();
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
		KryoMasterEndpoint kryoEndpoint = new KryoMasterEndpoint();
		kryoEndpoint.setConnection(connection);

		try {
			DefaultMasterImpl masterEndpoint = buildMaster(kryoEndpoint);
			masters.put(connection, masterEndpoint);
			zeroLock.increment();
		} catch (Exception e) {
			LOG.error("Could not start connection", e);
		}
	}

	public void disconnected(Connection connection) {
		zeroLock.decrement();
		DefaultMasterImpl masterEndpoint = masters.get(connection);
		masters.remove(masterEndpoint);
		Task<?> task = masterEndpoint.getCurrentTask();
		if (task != null) {
			try {
				new TaskEndpointImpl<>(queueName).reschedule(task
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
					DefaultMasterImpl endpointImpl = masters.get(connection);

					Message<?> inputMessage = (Message<?>) object;
					KryoMasterEndpoint endpoint = (KryoMasterEndpoint) endpointImpl
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

	private DefaultMasterImpl buildMaster(KryoMasterEndpoint kryoEndpoint)
			throws Exception {
		String queueName = masterClass.getAnnotation(SocketMaster.class).queueName();
		Master master = masterClass.newInstance();
		return DefaultMasterImpl.newInstance(kryoEndpoint,
				queueName, master.getRegistrationObject());
	}
}
