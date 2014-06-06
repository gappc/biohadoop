package at.ac.uibk.dps.biohadoop.persistencemanager.file;

import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceSaveConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileSaveConfiguration implements PersistenceSaveConfiguration {

	private final String path;
	private final int afterIterations;

	public FileSaveConfiguration(String path, int afterIterations) {
		this.path = path;
		this.afterIterations = afterIterations;
	}
	
	@JsonCreator
	public static FileSaveConfiguration create(
			@JsonProperty("path") String path,
			@JsonProperty("afterIterations") int afterIterations) {
		return new FileSaveConfiguration(path, afterIterations);
	}

	public String getPath() {
		return path;
	}
	
	@Override
	public int getAfterIterations() {
		return afterIterations;
	}

}
