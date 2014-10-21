package at.ac.uibk.dps.biohadoop.tasksystem.worker;


public interface Worker {

	public void start(String host, int port) throws WorkerException, ConnectionRefusedException;

}
