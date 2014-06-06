package at.ac.uibk.dps.biohadoop.persistencemanager;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@persistenceLoadConfigurationClass")
public interface PersistenceLoadConfiguration {

	public boolean isOnStartup();
}
