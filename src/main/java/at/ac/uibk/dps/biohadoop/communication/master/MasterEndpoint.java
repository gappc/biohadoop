package at.ac.uibk.dps.biohadoop.communication.master;


public interface MasterEndpoint {

	public void configure(String queueName) throws MasterException;

	public void start() throws MasterException;

	public void stop();
	
}
