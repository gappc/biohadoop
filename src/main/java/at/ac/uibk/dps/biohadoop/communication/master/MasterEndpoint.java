package at.ac.uibk.dps.biohadoop.communication.master;


public interface MasterEndpoint {

	public void configure(String settingName) throws MasterException;

	public void start() throws MasterException;

	public void stop();
	
}
