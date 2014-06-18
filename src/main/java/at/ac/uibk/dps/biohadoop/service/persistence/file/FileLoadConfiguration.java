package at.ac.uibk.dps.biohadoop.service.persistence.file;

import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceLoadConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileLoadConfiguration implements PersistenceLoadConfiguration {

	private final String path;
	private final boolean onStartup;

	public FileLoadConfiguration(String path, boolean onStartup) {
		this.path = path;
		this.onStartup = onStartup;
	}

	@JsonCreator
	public static FileLoadConfiguration create(
			@JsonProperty("path") String path,
			@JsonProperty("onStartup") boolean onStartup) {
		return new FileLoadConfiguration(path, onStartup);
	}
	
	public String getPath() {
		return path;
	}

	public boolean isOnStartup() {
		return onStartup;
	}

}
