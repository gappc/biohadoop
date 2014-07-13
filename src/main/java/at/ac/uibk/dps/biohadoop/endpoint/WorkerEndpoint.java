package at.ac.uibk.dps.biohadoop.endpoint;


public interface WorkerEndpoint<T, S> {

	public void run(String host, int port) throws Exception;
	public Class<? extends MasterEndpoint> getMasterEndpoint();
	public void readRegistrationObject(Object data);
	public S compute(T data);
	
}
