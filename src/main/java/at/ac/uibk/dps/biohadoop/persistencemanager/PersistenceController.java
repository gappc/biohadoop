package at.ac.uibk.dps.biohadoop.persistencemanager;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;

public interface PersistenceController {

	public ApplicationData<?> load(ApplicationId applicationId) throws PersistenceLoadException;
	public void save(ApplicationId applicationId) throws PersistenceSaveException;
}
