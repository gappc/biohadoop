package at.ac.uibk.dps.biohadoop.communication.worker;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;


public interface WorkerEndpoint<T, S> {

	public void run(String host, int port) throws WorkerException;
	public Class<? extends MasterEndpoint> getMasterEndpoint();
	public void readRegistrationObject(Object data);
	public S compute(T data);
	
}
