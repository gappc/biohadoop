package at.ac.uibk.dps.biohadoop.tasksystem.adapter;


public interface Adapter {

	public void configure(String pipelineName) throws AdapterException;

	public void start() throws AdapterException;

	public void stop();
	
}
