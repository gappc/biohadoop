package at.ac.uibk.dps.biohadoop.service.persistence.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceConfiguration;
import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceController;
import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceLoadException;
import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceSaveException;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;
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
	public SolverData<?> load(SolverId solverId)
			throws PersistenceLoadException {
		SolverService solverService = SolverService
				.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		PersistenceConfiguration persistenceConfiguration = solverConfiguration
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
					"Loading data for solver with name {} and solverId {} from {}",
					solverConfiguration.getName(), solverId,
					mostRecentFile);

			InputStream is = HdfsUtil.openFile(yarnConfiguration,
					mostRecentFile);
			BufferedInputStream bis = new BufferedInputStream(is);
			SolverData<?> solverData = objectMapper.readValue(bis,
					SolverData.class);

			saveLoadInformation(solverId, solverData, mostRecentFile);

			return solverData;
		} catch (IOException e) {
			throw new PersistenceLoadException(
					"Could not load solver data from path: "
							+ mostRecentFile, e);
		}
	}

	private void saveLoadInformation(SolverId solverId,
			SolverData<?> solverData, String mostRecentFile)
			throws PersistenceLoadException {
		SolverService solverService = SolverService
				.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		PersistenceConfiguration persistenceConfiguration = solverConfiguration
				.getPersistenceConfiguration();
		FileSaveConfiguration configuration = (FileSaveConfiguration) persistenceConfiguration
				.saveConfiguration();
		String path = configuration.getPath();

		String savePath = getSavePath(solverId, path,
				solverConfiguration.getName());

		String fullPath = savePath + ".startupLoadingInfo";

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			FileLoadInformation fileLoadInformation = new FileLoadInformation(
					"Continue work with existing data", mostRecentFile, solverData);
			objectMapper.writeValue(bos, fileLoadInformation);
		} catch (IOException e) {
			throw new PersistenceLoadException(
					"Could not save startupLoadingInfo to file: " + fullPath, e);
		}
	}

	@Override
	public void save(SolverId solverId)
			throws PersistenceSaveException {
		SolverService solverService = SolverService
				.getInstance();
		SolverData<?> solverData = solverService
				.getSolverData(solverId);
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		PersistenceConfiguration persistenceConfiguration = solverConfiguration
				.getPersistenceConfiguration();
		FileSaveConfiguration configuration = (FileSaveConfiguration) persistenceConfiguration
				.saveConfiguration();
		String path = configuration.getPath();

		String savePath = getSavePath(solverId, path,
				solverConfiguration.getName());

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.isDirectory(yarnConfiguration, savePath)) {
				if (!HdfsUtil.mkDir(yarnConfiguration, savePath)) {
					throw new PersistenceSaveException(
							"Could not create directory with path " + savePath);
				}
			}

			String fullPath = savePath + solverConfiguration.getName()
					+ "_" + solverId.toString() + "_"
					+ solverData.getIteration() + "_"
					+ solverData.getTimestamp();

			if (HdfsUtil.exists(yarnConfiguration, fullPath)) {
				throw new PersistenceSaveException("File " + fullPath
						+ " already exists");
			}

			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			objectMapper.writeValue(bos, solverData);
		} catch (IOException e) {
			throw new PersistenceSaveException(
					"Could not save solver with name "
							+ solverConfiguration.getName()
							+ " and solverId " + solverId
							+ " to file: " + savePath, e);
		}
	}

	private String getSavePath(SolverId solverId, String path,
			String solverName) {
		String savePath = path;
		if (savePath.charAt(savePath.length() - 1) != '/') {
			savePath += "/";
		}

		savePath += solverName + "/" + solverId + "/";

		return savePath;
	}
}
