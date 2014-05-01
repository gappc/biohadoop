package at.ac.uibk.dps.biohadoop.hadoop;

public interface Launcher {

	public Config getConfiguration(String configurationFile);
	public boolean isConfigurationValid(String configFilename);
	public void launch(String configFilename) throws LaunchException;
	
}
