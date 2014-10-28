package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;


public interface Worker {

	public void start(String host, int port) throws WorkerException, ConnectionRefusedException;

}
