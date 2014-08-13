package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.utils.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class DefaultKryoConnection<R, T, S> extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultKryoConnection.class);

	private final Map<Connection, DefaultMasterImpl<R, T, S>> masters = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();

	private final Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass;
	private final String queueName;

	public DefaultKryoConnection(
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass,
			String path) {
		this.remoteExecutableClass = remoteExecutableClass;
		this.queueName = path;
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
			DefaultMasterImpl<R, T, S> masterEndpoint = buildMaster();
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
					try {
						DefaultMasterImpl<R, T, S> masterEndpoint = masters
								.get(connection);

						Message<S> inputMessage = (Message<S>) object;
						Message<T> outputMessage = masterEndpoint.handleMessage(inputMessage);

						connection.sendTCP(outputMessage);

						return 0;
					} catch (Exception e) {
						LOG.error("Error", e);
					}
					return 1;
				}
			});
		}
	}

	private DefaultMasterImpl<R, T, S> buildMaster()
			throws Exception {
		// String queueName = masterClass.getAnnotation(KryoMaster.class)
		// .queueName();
		return new DefaultMasterImpl<R, T, S>(queueName);
	}

	private Object getRegistrationObject(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
				.forName(className);
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		return master.getInitalData();
	}

	private Message<?> handleRegistration(DefaultMasterImpl masterEndpoint,
			Message<?> inputMessage) throws InstantiationException,
			IllegalAccessException, IOException, ClassNotFoundException {
//		ClassNameWrapper<String> wrapper = (ClassNameWrapper<String>) inputMessage
//				.getTask().getData();
//		String className = wrapper.getWrapped();
//
//		Object registrationObject = getRegistrationObject(className);
//
//		Message<?> registrationMessage = masterEndpoint
//				.handleRegistration(registrationObject);
//		return ClassNameWrapperUtils
//				.wrapMessage(registrationMessage, className);
		return null;
	}

}
