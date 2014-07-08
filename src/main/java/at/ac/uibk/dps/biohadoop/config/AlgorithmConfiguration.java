package at.ac.uibk.dps.biohadoop.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@algorithmConfigClass")
public interface AlgorithmConfiguration {
	
}
