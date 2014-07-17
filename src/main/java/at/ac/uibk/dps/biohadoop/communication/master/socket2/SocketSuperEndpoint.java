package at.ac.uibk.dps.biohadoop.communication.master.socket2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationException;
import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.MessageType;
import at.ac.uibk.dps.biohadoop.communication.master.DefaultMasterImpl;
import at.ac.uibk.dps.biohadoop.communication.master.MasterSendReceive;
import at.ac.uibk.dps.biohadoop.communication.master.ReceiveException;
import at.ac.uibk.dps.biohadoop.communication.master.SendException;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;

public class SocketSuperEndpoint implements Callable<Integer>, MasterSendReceive {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketSuperEndpoint.class);

	private final String className = ClassnameProvider.getClassname(SocketSuperEndpoint.class);
	private final Socket socket;
	private final String queueName;
	private final Object registrationObject;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;

	public SocketSuperEndpoint(Socket socket, String queueName, Object registrationObject) {
		this.socket = socket;
		this.queueName = queueName;
		this.registrationObject = registrationObject;
	}

	@Override
	public Integer call() {
		DefaultMasterImpl endpoint = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			endpoint = buildMaster();
			endpoint.handleRegistration();
			endpoint.handleWorkInit();
			while (!close) {
				endpoint.handleWork();
			}
		} catch (IOException e) {
			LOG.error("Error while running {}", className, e);
		} catch (CommunicationException e) {
			LOG.error("Error while communicating with worker, closing communication", e);
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

	@SuppressWarnings("unchecked")
	public <T> Message<T> receive() throws ReceiveException {
		try {
			return (Message<T>) is.readUnshared();
		} catch (ClassNotFoundException | IOException e) {
			LOG.error("Error while receiving", e);
			throw new ReceiveException(e);
		}
	}

	public <T> void send(Message<T> message) throws SendException {
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
	
	private DefaultMasterImpl buildMaster() {
		return DefaultMasterImpl.newInstance(this, queueName, registrationObject);
	}
}
