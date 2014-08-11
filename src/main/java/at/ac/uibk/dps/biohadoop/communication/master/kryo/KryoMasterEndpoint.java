package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedKryo;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoMasterEndpoint extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoMasterEndpoint.class);

	private final Map<Connection, DefaultMasterImpl> masters = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();
	private final Class<? extends RemoteExecutable<?, ?, ?>> masterClass;
	private final String queueName;

	public KryoMasterEndpoint(Class<? extends RemoteExecutable<?, ?, ?>> masterClass) {
		this.masterClass = masterClass;
		queueName = masterClass.getAnnotation(DedicatedKryo.class).queueName();
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
		try {
			DefaultMasterImpl masterEndpoint = buildMaster();
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
				new TaskEndpointImpl<>(queueName).reschedule(task.getTaskId());
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
					Message<?> outputMessage = null;
					
					if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
						try {
							RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
							outputMessage = endpointImpl.handleRegistration(master
									.getInitalData());
						} catch (InstantiationException
								| IllegalAccessException e) {
							LOG.error("Could net get registration object from {}", masterClass, e);
							return 1;
						}
					}
					if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
						outputMessage = endpointImpl.handleWorkInit();
					}
					if (inputMessage.getType() == MessageType.WORK_REQUEST) {
						outputMessage = endpointImpl.handleWork(inputMessage);
					}
					connection.sendTCP(outputMessage);

					return 0;
				}
			});
		}
	}

	private DefaultMasterImpl buildMaster()
			throws Exception {
		String queueName = masterClass.getAnnotation(DedicatedKryo.class)
				.queueName();
		return DefaultMasterImpl.newInstance(queueName);
	}
}
