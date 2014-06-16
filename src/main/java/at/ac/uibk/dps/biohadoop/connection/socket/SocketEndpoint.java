package at.ac.uibk.dps.biohadoop.connection.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.MessageType;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class SocketEndpoint implements Runnable, Endpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketEndpoint.class);

	private final String className = Helper.getClassname(SocketEndpoint.class);
	private final Socket socket;
	private final MasterConfiguration masterConfiguration;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;
	
	public SocketEndpoint(Socket socket, MasterConfiguration masterConfiguration) {
		this.socket = socket;
		this.masterConfiguration = masterConfiguration;
	}

	@Override
	public void run() {
		JobManager<?, ?> jobManager = JobManager.getInstance();
		
		MasterEndpoint endpoint = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			endpoint = buildMaster(masterConfiguration.getMasterEndpoint());
			endpoint.handleRegistration();
			endpoint.handleWorkInit();
			while (!close) {
				endpoint.handleWork();
			}
//		} catch (ShutdownException e) {
//			LOG.info("Got shutdown event");
		} catch (Exception e) {
			LOG.error("Error while running {}", className, e);
			if (endpoint != null) {
				Task currentTask = endpoint.getCurrentTask();
				if (currentTask != null) {
					// TODO make queueName dynamic
					boolean hasRescheduled = jobManager.reschedule(currentTask,
							masterConfiguration.getQueueName());
					if (!hasRescheduled) {
						LOG.error("Could not reschedule task at {}",
								currentTask);
					}
				}
			}
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					LOG.error("Error while closing OutputStream", e);
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("Error while closing InputStream", e);
				}
			}
		}
	}

	private MasterEndpoint buildMaster(Class<? extends MasterEndpoint> masterEndpointClass) throws Exception {
		try {
			Constructor<? extends MasterEndpoint> constructor = masterEndpointClass.getDeclaredConstructor(Endpoint.class);
			return constructor.newInstance(this);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOG.error("Could not instanciate new {} with parameter {}", masterEndpointClass, this);
			throw new Exception(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Message<T> receive() throws ReceiveException {
		try {
			return (Message<T>) is.readUnshared();
		} catch (ClassNotFoundException | IOException e) {
			LOG.error("Error while receiving", e);
			throw new ReceiveException(e);
		}
	}

	public void send(Message<?> message) throws SendException {
		try {
			counter++;
			if (counter % 10000 == 0) {
				counter = 0;
				os.reset();
			}
			os.writeUnshared(message);
			os.flush();
			if (message.getType() == MessageType.SHUTDOWN) {
				close = true;
			}
		} catch (IOException e) {
			LOG.error("Error while sending", e);
			throw new SendException(e);
		}
	}
}
