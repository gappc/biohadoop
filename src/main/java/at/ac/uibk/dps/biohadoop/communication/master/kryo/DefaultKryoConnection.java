package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.queue.Task;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskException;
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

	private final String queueName;

	public DefaultKryoConnection(String queueName) {
		this.queueName = queueName;
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
		DefaultMasterImpl<R, T, S> masterEndpoint = masters.get(connection);
		masters.remove(masterEndpoint);
		Task<?> task = masterEndpoint.getCurrentTask();
		if (task != null) {
			try {
				new TaskEndpointImpl<>(queueName).reschedule(task.getTaskId());
			} catch (TaskException | ShutdownException e) {
				LOG.error("Error while rescheduling task", e);
			}
		}
	}

	public void received(final Connection connection, final Object object) {
		if (object instanceof Message) {

			// TODO SEVERE: Exception is swallowed!!!
			executorService.submit(new Callable<Object>() {

				@Override
				public Object call() throws MasterException {
					try {
						DefaultMasterImpl<R, T, S> masterEndpoint = masters
								.get(connection);

						@SuppressWarnings("unchecked")
						Message<S> inputMessage = (Message<S>) object;
						Message<T> outputMessage = masterEndpoint
								.handleMessage(inputMessage);

						connection.sendTCP(outputMessage);

						return null;
					} catch (Exception e) {
						throw new MasterException("Error in communication", e);
					}
				}
			});
		}
	}

	private DefaultMasterImpl<R, T, S> buildMaster() throws Exception {
		return new DefaultMasterImpl<R, T, S>(queueName);
	}

}
