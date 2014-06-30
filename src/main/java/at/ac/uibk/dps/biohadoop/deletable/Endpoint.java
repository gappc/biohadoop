package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.endpoint.ReceiveException;
import at.ac.uibk.dps.biohadoop.endpoint.SendException;

public interface Endpoint {
	public <T> Message<T> receive() throws ReceiveException;
	public void send(Message<?> message) throws SendException;
}
