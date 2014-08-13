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
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;

public class DefaultSocketConnection<R, T, S> implements Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultSocketConnection.class);

	private final Socket socket;
	private final String path;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;

	public DefaultSocketConnection(Socket socket,
			Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass,
			String path) {
		this.socket = socket;
		this.path = path;
	}

	@Override
	public Object call() throws MasterException {
		DefaultMasterImpl<R, T, S> masterEndpoint = null;
		try {
			LOG.info("Opened Socket on server");

			os = new ObjectOutputStream(new BufferedOutputStream(
					socket.getOutputStream()));
			os.flush();
			is = new ObjectInputStream(new BufferedInputStream(
					socket.getInputStream()));

			masterEndpoint = buildMaster();

			while (!close && !ShutdownWaitingService.isFinished()) {
				Message<S> inputMessage = receive();
				Message<T> outputMessage = masterEndpoint
						.handleMessage(inputMessage);
				send(outputMessage);
			}
		} catch (IOException | InstantiationException | IllegalAccessException
				| ClassNotFoundException | HandleMessageException e) {
			// TODO remove logging
			LOG.error("Could not handle worker request", e);
			throw new MasterException("Could not handle worker request", e);
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

	private DefaultMasterImpl<R, T, S> buildMaster()
			throws InstantiationException, IllegalAccessException {
		return new DefaultMasterImpl<R, T, S>(path);
	}
}
