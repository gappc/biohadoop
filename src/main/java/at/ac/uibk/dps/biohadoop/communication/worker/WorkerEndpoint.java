package at.ac.uibk.dps.biohadoop.communication.worker;


public interface WorkerEndpoint {

	public void configure(String[] args) throws WorkerException;

	public void start() throws WorkerException;
	
}
