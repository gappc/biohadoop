package at.ac.uibk.dps.biohadoop.endpoint;

import at.ac.uibk.dps.biohadoop.connection.Message;

public interface MasterSendReceive {

	public <T>void send(Message<T> message) throws SendException;

	public <T>Message<T> receive() throws ReceiveException;

}
