package at.ac.uibk.dps.biohadoop.persistencemanager;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationHandler;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PersistenceManager implements ApplicationHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(PersistenceManager.class);

	private static PersistenceManager PERSISTENCE_MANAGER = new PersistenceManager();
	private ObjectMapper objectMapper;

	private PersistenceManager() {
	}

	public static PersistenceManager getInstance() {
		return PersistenceManager.PERSISTENCE_MANAGER;
	}

	public void persist(ApplicationId applicationId) {
		String filename = "/tmp/ga.save";
		try {

			OutputStream os = HdfsUtil.createFile(new YarnConfiguration(),
					filename);

			ApplicationData<?> applicationData = ApplicationManager
					.getInstance().getApplicationData(applicationId);
			objectMapper.writeValue(os, applicationData);
		} catch (IOException e) {
			LOG.error("Could not save data for applicationId {} to file {}",
					applicationId, filename, e);
		}
	}

	public void onNew(ApplicationId applicationId) {
		LOG.debug("onNew for applcation {}", applicationId);

		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);

		PersistenceConfiguration persistenceConfiguration = applicationConfiguration
				.getPersistenceConfiguration();
		boolean onStartup = persistenceConfiguration.loadConfiguration()
				.isOnStartup();

		if (onStartup) {
			LOG.info(
					"Start loading data for application with name {} and applicationId {}",
					applicationConfiguration.getName(), applicationId);
			PersistenceController persistenceController = persistenceConfiguration
					.getPersistenceController();
			try {
				ApplicationData<?> applicationData = persistenceController
						.load(applicationId);
				applicationManager.setApplicationData(applicationId,
						applicationData, true);
				LOG.info(
						"Successful loading data for application with name {} and applicationId {}",
						applicationConfiguration.getName(), applicationId);
			} catch (PersistenceLoadException e) {
				LOG.error(
						"Error while trying to load startup data for application {}",
						applicationId, e);
				// TODO decide what to do in case of load error
				// a) abort
				// b) just print message
				// c) ...?
			}
		}
	}

	@Override
	public void onDataUpdate(ApplicationId applicationId) {
		LOG.debug("onDataUpdate for applcation {}", applicationId);

		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);
		ApplicationData<?> applicationData = applicationManager
				.getApplicationData(applicationId);

		PersistenceConfiguration persistenceConfiguration = applicationConfiguration
				.getPersistenceConfiguration();
		int saveAfterEveryIteration = persistenceConfiguration
				.saveConfiguration().getAfterIterations();
		
		if (applicationData.getIteration() % saveAfterEveryIteration == 0) {
			LOG.info(
					"Persisting data for application with name {} and applicationId {}",
					applicationConfiguration.getName(), applicationId);
			PersistenceController persistenceController = persistenceConfiguration
					.getPersistenceController();
			try {
				persistenceController.save(applicationId);
			} catch (PersistenceSaveException e) {
				LOG.error(
						"Error while trying to persist data for application {}",
						applicationId, e);
				// TODO decide what to do in case of save error
				// a) abort
				// b) just print message
				// c) ...?
			}
		}
	}
}
