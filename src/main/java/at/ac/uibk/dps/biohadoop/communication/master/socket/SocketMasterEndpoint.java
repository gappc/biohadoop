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
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;

public class SocketMasterEndpoint implements Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketMasterEndpoint.class);

	private final String className = ClassnameProvider
			.getClassname(SocketMasterEndpoint.class);
	private final Socket socket;
	private final Class<? extends RemoteExecutable<?, ?, ?>> masterClass;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;

	public SocketMasterEndpoint(Socket socket,
			Class<? extends RemoteExecutable<?, ?, ?>> masterClass) {
		this.socket = socket;
		this.masterClass = masterClass;
	}

	@Override
	public Integer call() {
		DefaultMasterImpl masterEndpoint = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			masterEndpoint = buildMaster();

			handleRegistration(masterEndpoint);
			handleWorkInit(masterEndpoint);
			while (!close) {
				handleWork(masterEndpoint);
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

	private void handleRegistration(DefaultMasterImpl masterEndpoint)
			throws InstantiationException, IllegalAccessException, IOException,
			ClassNotFoundException {
		receive();
		RemoteExecutable<?, ?, ?> master = masterClass.newInstance();
		Message<?> outputMessage = masterEndpoint.handleRegistration(master.getInitalData());
		send(outputMessage);
	}
	
	private void handleWorkInit(DefaultMasterImpl masterEndpoint) throws ClassNotFoundException, IOException {
		receive();
		Message<?> outputMessage = masterEndpoint.handleWorkInit();
		send(outputMessage);
	}
	
	private void handleWork(DefaultMasterImpl masterEndpoint) throws IOException, ClassNotFoundException {
		Message<?> inputMessage = receive();
		Message<?> outputMessage = masterEndpoint.handleWork(inputMessage);
		send(outputMessage);
	}

	@SuppressWarnings("unchecked")
	private <T> Message<T> receive() throws ClassNotFoundException, IOException {
		return (Message<T>) is.readUnshared();
	}

	private <T> void send(Message<T> message) throws IOException {
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

	private DefaultMasterImpl buildMaster() throws InstantiationException,
			IllegalAccessException {
//		String queueName = masterClass.getAnnotation(DedicatedSocket.class)
//				.queueName();
//		return DefaultMasterImpl.newInstance(queueName);
		return null;
	}
}
