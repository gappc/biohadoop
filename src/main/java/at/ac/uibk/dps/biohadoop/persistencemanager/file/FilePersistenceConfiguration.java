package at.ac.uibk.dps.biohadoop.persistencemanager.file;

import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceController;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceLoadConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceSaveConfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class FilePersistenceConfiguration implements PersistenceConfiguration {

	private final PersistenceSaveConfiguration saveConfiguration;
	private final PersistenceLoadConfiguration loadConfiguration;

	public FilePersistenceConfiguration(
			PersistenceSaveConfiguration saveConfiguration,
			PersistenceLoadConfiguration loadConfiguration) {
		this.saveConfiguration = saveConfiguration;
		this.loadConfiguration = loadConfiguration;
	}

	@JsonCreator
	public static FilePersistenceConfiguration create(
			@JsonProperty("saveConfiguration") PersistenceSaveConfiguration saveConfiguration,
			@JsonProperty("loadConfiguration") PersistenceLoadConfiguration loadConfiguration) {
		return new FilePersistenceConfiguration(saveConfiguration,
				loadConfiguration);
	}

	public PersistenceSaveConfiguration saveConfiguration() {
		return saveConfiguration;
	}

	public PersistenceLoadConfiguration loadConfiguration() {
		return loadConfiguration;
	}

	@Override
	@JsonIgnore
	public PersistenceController getPersistenceController() {
		return new FilePersistenceController();
	}

}
