package at.ac.uibk.dps.biohadoop.handler;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@handlerConfigurationClass")
public interface HandlerConfiguration {

	public Class<? extends Handler> getHandler();
	
}
