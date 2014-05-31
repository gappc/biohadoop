package at.ac.uibk.dps.biohadoop.endpoint;

import at.ac.uibk.dps.biohadoop.jobmanager.remote.Message;

public interface Endpoint {
	public <T> Message<T> receive() throws ReceiveException;
	public void send(Message<?> message) throws SendException;
}
