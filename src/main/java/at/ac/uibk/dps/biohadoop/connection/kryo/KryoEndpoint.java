package at.ac.uibk.dps.biohadoop.connection.kryo;

import at.ac.uibk.dps.biohadoop.connection.Message;
import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;

import com.esotericsoftware.kryonet.Connection;

public class KryoEndpoint implements Endpoint {

	private Message<?> inputMessage;
	private Connection connection;
	
	public void setInputMessage(Message<?> inputMessage) {
		this.inputMessage = inputMessage;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Message<T> receive() throws ReceiveException {
		return (Message<T>)inputMessage;
	}

	@Override
	public <T>void send(Message<T> message) throws SendException {
		try {
			connection.sendTCP(message);
		} catch (Exception e) {
			throw new SendException("Could not send message", e);
		}
	}

}
