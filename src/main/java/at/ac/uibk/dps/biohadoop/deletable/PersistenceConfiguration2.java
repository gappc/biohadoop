package at.ac.uibk.dps.biohadoop.deletable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@persistenceConfigurationClass")
public interface PersistenceConfiguration2 {
	
	public PersistenceLoadConfiguration2 loadConfiguration();
	public PersistenceSaveConfiguration2 saveConfiguration();
	public PersistenceController2 getPersistenceController();
	
}
