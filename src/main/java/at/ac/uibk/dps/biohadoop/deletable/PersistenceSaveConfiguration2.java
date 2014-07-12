package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@persistenceSaveConfigurationClass")
public interface PersistenceSaveConfiguration2 extends HandlerConfiguration {

	public int getAfterIterations();
	
}
