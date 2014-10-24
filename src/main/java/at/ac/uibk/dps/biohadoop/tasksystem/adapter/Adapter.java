package at.ac.uibk.dps.biohadoop.tasksystem.adapter;


public interface Adapter {

	public void start(String pipelineName) throws AdapterException;

	public void stop() throws AdapterException;
	
}
