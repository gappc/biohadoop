package at.ac.uibk.dps.biohadoop.connection.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointImpl;
import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.connection.MessageType;
import at.ac.uibk.dps.biohadoop.endpoint.CommunicationException;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.Master;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.torename.Helper;

public class SocketEndpoint implements Callable<Integer>, Endpoint {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketEndpoint.class);

	private final String className = Helper.getClassname(SocketEndpoint.class);
	private final Socket socket;
	private final Master master;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;

	public SocketEndpoint(Socket socket, Master master) {
		this.socket = socket;
		this.master = master;
	}

	@Override
	public Integer call() {
		DefaultEndpointImpl endpoint = null;
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
	
	private DefaultEndpointImpl buildMaster() {
		return DefaultEndpointImpl.newInstance(this, master.getQueueName(), master.getRegistrationObject());
	}
}
