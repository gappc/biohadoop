package at.ac.uibk.dps.biohadoop.tasksystem.adapter.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.MessageType;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.HandleMessageException;

public class SocketConnection<R, T, S> implements Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(SocketConnection.class);

	private final Socket socket;
	private final String path;

	private ObjectOutputStream os = null;
	private ObjectInputStream is = null;
	private int counter = 0;
	private boolean close = false;

	public SocketConnection(Socket socket, String path) {
		this.socket = socket;
		this.path = path;
	}

	@Override
	public Object call() throws AdapterException {
//		TaskConsumer<R, T, S> taskConsumer = null;
//		try {
//			LOG.info("Opened Socket on server");
//
//			os = new ObjectOutputStream(new BufferedOutputStream(
//					socket.getOutputStream()));
//			os.flush();
//			is = new ObjectInputStream(new BufferedInputStream(
//					socket.getInputStream()));
//
//			taskConsumer = new TaskConsumer<R, T, S>(path);
//
//			while (!close && !ShutdownWaitingService.isFinished()) {
//				Message<S> inputMessage = receive();
//				Message<T> outputMessage = taskConsumer
//						.handleMessage(inputMessage);
//				send(outputMessage);
//			}
//		} catch (IOException | ClassNotFoundException | HandleMessageException e) {
//			// TODO remove logging
//			LOG.error("Could not handle worker request", e);
//			throw new AdapterException("Could not handle worker request", e);
//		} finally {
//			if (os != null) {
//				try {
//					os.close();
//				} catch (IOException e) {
//					LOG.error("Error while closing OutputStream", e);
//				}
//			}
//			if (is != null) {
//				try {
//					is.close();
//				} catch (IOException e) {
//					LOG.error("Error while closing InputStream", e);
//				}
//			}
//		}
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
		if (message.getType() == MessageType.SHUTDOWN.ordinal()) {
			close = true;
		}
	}
}
