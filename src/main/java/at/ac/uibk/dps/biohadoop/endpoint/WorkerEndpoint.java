package at.ac.uibk.dps.biohadoop.endpoint;


public interface WorkerEndpoint<T, S> {

	public void run(String host, int port) throws Exception;
//	public String getMasterPrefix();
	public Class<? extends Master> getMasterEndpoint();
	public void readRegistrationObject(Object data);
	public S compute(T data);
	
}
