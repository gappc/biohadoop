package at.ac.uibk.dps.biohadoop.handler;

public interface HandlerClient {

	public void invokeDefaultHandlers();
	
	public void invokeHandlers(String operation, Object data);

}
