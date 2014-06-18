package at.ac.uibk.dps.biohadoop.endpoint;

import at.ac.uibk.dps.biohadoop.service.job.remote.Message;

public interface Endpoint {
	public <T> Message<T> receive() throws ReceiveException;
	public void send(Message<?> message) throws SendException;
}
