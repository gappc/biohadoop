package at.ac.uibk.dps.biohadoop.persistencemanager;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;

public class PersistenceManager {

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
}
