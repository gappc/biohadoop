package at.ac.uibk.dps.biohadoop.persistencemanager.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceController;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceLoadException;
import at.ac.uibk.dps.biohadoop.persistencemanager.PersistenceSaveException;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class FilePersistenceController implements PersistenceController {

	private static final Logger LOG = LoggerFactory
			.getLogger(FilePersistenceController.class);

	private ObjectMapper objectMapper = new ObjectMapper();

	public FilePersistenceController() {
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Override
	public ApplicationData<?> load(ApplicationId applicationId)
			throws PersistenceLoadException {
		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);
		PersistenceConfiguration persistenceConfiguration = applicationConfiguration
				.getPersistenceConfiguration();
		FileLoadConfiguration configuration = (FileLoadConfiguration) persistenceConfiguration
				.loadConfiguration();
		String path = configuration.getPath();

		String mostRecentFile = path;

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.exists(yarnConfiguration, path)) {
				throw new PersistenceLoadException("File/Path does not exist: "
						+ path);
			}
			if (HdfsUtil.isDirectory(yarnConfiguration, path)) {
				mostRecentFile = HdfsUtil.getMostRecentFileInPath(
						yarnConfiguration, path);
			}
			if (!HdfsUtil.exists(yarnConfiguration, mostRecentFile)) {
				throw new PersistenceLoadException(
						"Found no file to load data in: " + path);
			}

			LOG.info(
					"Loading data for application with name {} and applicationId {} from {}",
					applicationConfiguration.getName(), applicationId,
					mostRecentFile);

			InputStream is = HdfsUtil.openFile(yarnConfiguration,
					mostRecentFile);
			BufferedInputStream bis = new BufferedInputStream(is);
			ApplicationData<?> applicationData = objectMapper.readValue(bis,
					ApplicationData.class);

			saveLoadInformation(applicationId, applicationData, mostRecentFile);

			return applicationData;
		} catch (IOException e) {
			throw new PersistenceLoadException(
					"Could not load application data from path: "
							+ mostRecentFile, e);
		}
	}

	private void saveLoadInformation(ApplicationId applicationId,
			ApplicationData<?> applicationData, String mostRecentFile)
			throws PersistenceLoadException {
		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);
		PersistenceConfiguration persistenceConfiguration = applicationConfiguration
				.getPersistenceConfiguration();
		FileSaveConfiguration configuration = (FileSaveConfiguration) persistenceConfiguration
				.saveConfiguration();
		String path = configuration.getPath();

		String savePath = getSavePath(applicationId, path,
				applicationConfiguration.getName());

		String fullPath = savePath + ".startupLoadingInfo";

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			FileLoadInformation fileLoadInformation = new FileLoadInformation(
					"Continue work with existing data", mostRecentFile, applicationData);
			objectMapper.writeValue(bos, fileLoadInformation);
		} catch (IOException e) {
			throw new PersistenceLoadException(
					"Could not save startupLoadingInfo to file: " + fullPath, e);
		}
	}

	@Override
	public void save(ApplicationId applicationId)
			throws PersistenceSaveException {
		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		ApplicationData<?> applicationData = applicationManager
				.getApplicationData(applicationId);
		ApplicationConfiguration applicationConfiguration = applicationManager
				.getApplicationConfiguration(applicationId);
		PersistenceConfiguration persistenceConfiguration = applicationConfiguration
				.getPersistenceConfiguration();
		FileSaveConfiguration configuration = (FileSaveConfiguration) persistenceConfiguration
				.saveConfiguration();
		String path = configuration.getPath();

		String savePath = getSavePath(applicationId, path,
				applicationConfiguration.getName());

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.isDirectory(yarnConfiguration, savePath)) {
				if (!HdfsUtil.mkDir(yarnConfiguration, savePath)) {
					throw new PersistenceSaveException(
							"Could not create directory with path " + savePath);
				}
			}

			String fullPath = savePath + applicationConfiguration.getName()
					+ "_" + applicationId.toString() + "_"
					+ applicationData.getIteration() + "_"
					+ applicationData.getTimestamp();

			if (HdfsUtil.exists(yarnConfiguration, fullPath)) {
				throw new PersistenceSaveException("File " + fullPath
						+ " already exists");
			}

			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			objectMapper.writeValue(bos, applicationData);
		} catch (IOException e) {
			throw new PersistenceSaveException(
					"Could not save application with name "
							+ applicationConfiguration.getName()
							+ " and applicationId " + applicationId
							+ " to file: " + savePath, e);
		}
	}

	private String getSavePath(ApplicationId applicationId, String path,
			String applicationName) {
		String savePath = path;
		if (savePath.charAt(savePath.length() - 1) != '/') {
			savePath += "/";
		}

		savePath += applicationName + "/" + applicationId + "/";

		return savePath;
	}
}
