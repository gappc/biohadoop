package at.ac.uibk.dps.biohadoop.applicationmanager;

public interface ApplicationHandler {

	public void onNew(ApplicationId applicationId);
	public void onDataUpdate(ApplicationId applicationId);

}
