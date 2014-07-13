package at.ac.uibk.dps.biohadoop.communication.master;

import at.ac.uibk.dps.biohadoop.communication.Message;

public interface MasterSendReceive {

	public <T>void send(Message<T> message) throws SendException;

	public <T>Message<T> receive() throws ReceiveException;

}
