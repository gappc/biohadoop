package at.ac.uibk.dps.biohadoop.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface AlgorithmConfiguration {

	public Object buildParameters() throws BuildParameterException;
	
}
