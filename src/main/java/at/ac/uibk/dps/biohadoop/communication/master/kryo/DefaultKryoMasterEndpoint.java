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
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapper;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrapperUtils;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class DefaultKryoMasterEndpoint extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultKryoMasterEndpoint.class);

	private final Map<Connection, DefaultMasterImpl> masters = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();

	private String queueName;

	public DefaultKryoMasterEndpoint(String path) {
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
					try {
						DefaultMasterImpl masterEndpoint = masters
								.get(connection);

						Message<?> inputMessage = (Message<?>) object;
						Message<?> outputMessage = null;

						if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
							outputMessage = handleRegistration(masterEndpoint,
									inputMessage);
							// try {
							// String className = (String) inputMessage
							// .getTask().getData();
							//
							// Object registrationObject =
							// getRegistrationObject(className);
							// outputMessage = endpointImpl
							// .handleRegistration(registrationObject);
							// ClassNameWrapper<?> unifiedTask = new
							// ClassNameWrapper(
							// className, outputMessage.getTask()
							// .getData());
							// Task<?> task = new Task(outputMessage
							// .getTask().getTaskId(), unifiedTask);
							// Message<ClassNameWrapper<?>> unifiedMessage = new
							// Message(
							// outputMessage.getType(), task);
							//
							// outputMessage = unifiedMessage;
							//
							// // outputMessage =
							// // endpointImpl.handleRegistration(master
							// // .getRegistrationObject());
							// } catch (InstantiationException
							// | IllegalAccessException e) {
							// LOG.error("Could net get registration object",
							// e);
							// return 1;
							// } catch (ClassNotFoundException e) {
							// // TODO Auto-generated catch block
							// e.printStackTrace();
							// }
						}
						if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
							outputMessage = masterEndpoint.handleWorkInit();
						}
						if (inputMessage.getType() == MessageType.WORK_REQUEST) {
							outputMessage = masterEndpoint
									.handleWork(inputMessage);
						}
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

	private DefaultMasterImpl buildMaster() throws Exception {
		// String queueName = masterClass.getAnnotation(KryoMaster.class)
		// .queueName();
		return DefaultMasterImpl.newInstance(queueName);
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

		ClassNameWrapper<String> wrapper = (ClassNameWrapper<String>) inputMessage
				.getTask().getData();
		String className = wrapper.getWrapped();

		Object registrationObject = getRegistrationObject(className);

		Message<?> registrationMessage = masterEndpoint
				.handleRegistration(registrationObject);
		return ClassNameWrapperUtils
				.wrapMessage(registrationMessage, className);
	}

}
