package at.ac.uibk.dps.biohadoop.hadoop;

public abstract class AbstractBiohadoopConfig implements Config {

	private String launcherClass;
	
	public String getLauncherClass() {
		return launcherClass;
	}
	public void setLauncherClass(String launcherClass) {
		this.launcherClass = launcherClass;
	}
}
