package at.ac.uibk.dps.biohadoop.persistencemanager.file;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;

public class FileLoadInformation {

	private final String message;
	private final String loadedFrom;
	private final ApplicationData<?> applicationData;

	public FileLoadInformation(String message, String loadedFrom,
			ApplicationData<?> applicationData) {
		this.message = message;
		this.loadedFrom = loadedFrom;
		this.applicationData = applicationData;
	}

	public String getLoadedFrom() {
		return loadedFrom;
	}

	public String getMessage() {
		return message;
	}

	public ApplicationData<?> getApplicationData() {
		return applicationData;
	}
}
