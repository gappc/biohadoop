package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;

public interface Config {

	public String getVersion();
	public String getLauncherClass();
	public List<String> getIncludePaths();
}