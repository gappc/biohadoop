package at.ac.uibk.dps.biohadoop.connection.kryo;

import com.esotericsoftware.kryonet.Connection;

import at.ac.uibk.dps.biohadoop.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;
import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;

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
	public void send(Message<?> message) throws SendException {
		connection.sendTCP(message);
	}

}
