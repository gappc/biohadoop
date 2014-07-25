package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import at.ac.uibk.dps.biohadoop.communication.Message;
import at.ac.uibk.dps.biohadoop.communication.master.MasterSendReceive;
import at.ac.uibk.dps.biohadoop.communication.master.ReceiveException;
import at.ac.uibk.dps.biohadoop.communication.master.SendException;

import com.esotericsoftware.kryonet.Connection;

public class KryoMasterEndpoint implements MasterSendReceive {

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
