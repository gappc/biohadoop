package at.ac.uibk.dps.biohadoop.communication.adapter;


public interface Adapter {

	public void configure(String settingName) throws AdapterException;

	public void start() throws AdapterException;

	public void stop();
	
}
