package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class FilePersistenceConfiguration2 implements HandlerConfiguration,
		PersistenceConfiguration2 {

	private final PersistenceSaveConfiguration2 saveConfiguration;
	private final PersistenceLoadConfiguration2 loadConfiguration;

	public FilePersistenceConfiguration2(
			PersistenceSaveConfiguration2 saveConfiguration,
			PersistenceLoadConfiguration2 loadConfiguration) {
		this.saveConfiguration = saveConfiguration;
		this.loadConfiguration = loadConfiguration;
	}

	@JsonCreator
	public static FilePersistenceConfiguration2 create(
			@JsonProperty("saveConfiguration") PersistenceSaveConfiguration2 saveConfiguration,
			@JsonProperty("loadConfiguration") PersistenceLoadConfiguration2 loadConfiguration) {
		return new FilePersistenceConfiguration2(saveConfiguration,
				loadConfiguration);
	}

	public PersistenceSaveConfiguration2 saveConfiguration() {
		return saveConfiguration;
	}

	public PersistenceLoadConfiguration2 loadConfiguration() {
		return loadConfiguration;
	}

	@Override
	@JsonIgnore
	public PersistenceController2 getPersistenceController() {
		return new FilePersistenceController2();
	}

	@Override
	public Class<? extends Handler> getHandler() {
		return PersistenceService2.class;
	}

}
