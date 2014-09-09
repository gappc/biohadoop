package at.ac.uibk.dps.biohadoop.communication.adapter.kryo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.communication.adapter.TaskConsumer;
import at.ac.uibk.dps.biohadoop.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.utils.ZeroLock;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

public class KryoConnection<R, T, S> extends Listener {

	private static final Logger LOG = LoggerFactory
			.getLogger(KryoConnection.class);

	private final Map<Connection, TaskConsumer<R, T, S>> taskConsumers = new ConcurrentHashMap<>();
	private final ExecutorService executorService = Executors
			.newCachedThreadPool();
	private final ZeroLock zeroLock = new ZeroLock();

	private final String settingName;

	public KryoConnection(String settingName) {
		this.settingName = settingName;
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
			TaskConsumer<R, T, S> taskConsumer = new TaskConsumer<R, T, S>(
					settingName);
			taskConsumers.put(connection, taskConsumer);
			zeroLock.increment();
		} catch (Exception e) {
			LOG.error("Could not start connection", e);
		}
	}

	public void disconnected(Connection connection) {
		zeroLock.decrement();
		TaskConsumer<R, T, S> taskConsumer = taskConsumers.get(connection);
		taskConsumers.remove(taskConsumer);
		rescheduleCurrentTask(taskConsumer);
	}

	public void received(final Connection connection, final Object object) {
		if (object instanceof Message) {

			// TODO SEVERE: Exception is swallowed!!!
			executorService.submit(new Callable<Object>() {

				@Override
				public Object call() throws AdapterException {
					try {
						TaskConsumer<R, T, S> taskConsumer = taskConsumers
								.get(connection);

						@SuppressWarnings("unchecked")
						Message<S> inputMessage = (Message<S>) object;
						Message<T> outputMessage = taskConsumer
								.handleMessage(inputMessage);

						connection.sendTCP(outputMessage);

						return null;
					} catch (Exception e) {
						LOG.error("Error in communication", e);
						throw new AdapterException("Error in communication", e);
					}
				}
			});
		}
	}

	private void rescheduleCurrentTask(TaskConsumer<?, ?, ?> taskConsumer) {
		Task<?> task = taskConsumer.getCurrentTask();
		if (task == null) {
			LOG.error("Could not reschedule null task");
			return;
		}
		try {
			LOG.info("Trying to reschedule task {}", task);
			taskConsumer.reschedule(task.getTaskId());
		} catch (ShutdownException e) {
			LOG.error(
					"Error while rescheduling task {}, got ShutdownException",
					task, e);
		}
	}

}
