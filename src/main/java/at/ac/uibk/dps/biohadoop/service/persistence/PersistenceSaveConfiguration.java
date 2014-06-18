package at.ac.uibk.dps.biohadoop.service.persistence;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@persistenceSaveConfigurationClass")
public interface PersistenceSaveConfiguration {

	public int getAfterIterations();
	
}
