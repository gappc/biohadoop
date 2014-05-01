package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfig implements Config {

	private String version;
	private String launcherClass;
	private List<String> includePaths = new ArrayList<String>();
	
	@Override
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@Override
	public String getLauncherClass() {
		return launcherClass;
	}
	public void setLauncherClass(String launcherClass) {
		this.launcherClass = launcherClass;
	}
	@Override
	public List<String> getIncludePaths() {
		return includePaths;
	}
	public void setIncludePaths(List<String> includePaths) {
		this.includePaths = includePaths;
	}
}
