package at.ac.uibk.dps.biohadoop.service.persistence;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@persistenceConfigurationClass")
public interface PersistenceConfiguration {
	
	public PersistenceLoadConfiguration loadConfiguration();
	public PersistenceSaveConfiguration saveConfiguration();
	public PersistenceController getPersistenceController();
	
}
