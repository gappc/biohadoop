package at.ac.uibk.dps.biohadoop.config;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;

public interface Algorithm<T, S> {

	public T compute(ApplicationId applicationId, S parameter) throws AlgorithmException;
	
}
