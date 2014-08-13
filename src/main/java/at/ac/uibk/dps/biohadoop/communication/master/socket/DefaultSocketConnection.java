package at.ac.uibk.dps.biohadoop.communication.master.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.HandleMessageException;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;

public class DefaultSocketConnection<R, T, S> implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultSocketConnection.class);

	private final String className = ClassnameProvider
			.getClassname(DefaultSocketConnection.class);
	private final Socket socket;
	private final Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass;
	private final String path;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;

	public DefaultSocketConnection(Socket socket,
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass,
			String path) {
		this.remoteExecutableClass = remoteExecutableClass;
		this.socket = socket;
		this.path = path;
	}

	@Override
	public Integer call() {
		DefaultMasterImpl<R, T, S> masterEndpoint = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			masterEndpoint = buildMaster();

			while (!close) {
				Message<S> inputMessage = receive();
				Message<T> outputMessage = masterEndpoint.handleMessage(inputMessage);
//				if (inputMessage.getType() == MessageType.REGISTRATION_REQUEST) {
//					outputMessage = handleRegistration(masterEndpoint,
//							inputMessage);
//				}
//				if (inputMessage.getType() == MessageType.WORK_INIT_REQUEST) {
//					outputMessage = masterEndpoint.handleWorkInit();
//				}
//				if (inputMessage.getType() == MessageType.WORK_REQUEST) {
//					outputMessage = masterEndpoint.handleWork(inputMessage);
//				}
				send(outputMessage);
			}
		} catch (IOException e) {
			LOG.error("Error while running {}", className, e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HandleMessageException e) {
			LOG.error("Could not handle worker request", e);
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
		return 0;
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

	@SuppressWarnings("unchecked")
	private Message<S> receive() throws ClassNotFoundException, IOException {
		return (Message<S>) is.readUnshared();
	}

	private void send(Message<?> message) throws IOException {
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
	}

	private DefaultMasterImpl<R, T, S> buildMaster() throws InstantiationException,
			IllegalAccessException {
		return new DefaultMasterImpl<R, T, S>(path);
	}

	private Object getRegistrationObject(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Class<? extends RemoteExecutable<?, ?, ?>> masterClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
				.forName(className);
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		return master.getInitalData();
	}
}
