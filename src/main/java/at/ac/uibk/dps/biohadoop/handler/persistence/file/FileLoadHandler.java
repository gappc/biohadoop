package at.ac.uibk.dps.biohadoop.handler.persistence.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.datastore.DataClient;
import at.ac.uibk.dps.biohadoop.datastore.DataClientImpl;
import at.ac.uibk.dps.biohadoop.datastore.DataOptions;
import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerBuilder;
import at.ac.uibk.dps.biohadoop.handler.HandlerConstants;
import at.ac.uibk.dps.biohadoop.handler.HandlerInitException;
import at.ac.uibk.dps.biohadoop.handler.UnknownHandlerException;
import at.ac.uibk.dps.biohadoop.handler.persistence.PersistenceLoadException;
import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileLoadHandler implements Handler {

	private static final Logger LOG = LoggerFactory
			.getLogger(FileLoadHandler.class);

	private final ObjectMapper objectMapper = new ObjectMapper();

	private SolverId solverId;
	private FileLoadConfiguration fileLoadConfiguration;

	@Override
	public void init(SolverId solverId) throws HandlerInitException {
		this.solverId = solverId;
		fileLoadConfiguration = getFileLoadConfiguration();
	}

	@Override
	public void update(String operation) {
		if (HandlerConstants.ALGORITHM_START.equals(operation)) {
			LOG.debug("{} for algorithm {}", HandlerConstants.ALGORITHM_START, solverId);
		
			if (fileLoadConfiguration.isOnStartup()) {
				LOG.info("Start loading data for solver {}", solverId);
				try {
					SolverData<?> solverData = load(solverId, fileLoadConfiguration);
					DataClient dataClient = new DataClientImpl(solverId);
					dataClient.setData(DataOptions.COMPUTATION_RESUMED, true);
					dataClient.setData(DataOptions.DATA, solverData.getData());
					dataClient.setData(DataOptions.FITNESS, solverData.getFitness());
					dataClient.setData(DataOptions.ITERATION_START, solverData.getIteration());
					dataClient.setData(DataOptions.TIMESTAMP, solverData.getTimestamp());
					dataClient.setData(DataOptions.TIMEZONE, solverData.getTimezone());
					LOG.info("Successful loading data for solver {}", solverId);
				} catch (PersistenceLoadException e) {
					LOG.error("Handler could not load data", e);
				}
			}
		}
	}

	private SolverData<?> load(SolverId solverId,
			FileLoadConfiguration fileLoadConfiguration)
			throws PersistenceLoadException {
		String path = fileLoadConfiguration.getPath();

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

			LOG.info("Loading data for solver {} from {}", solverId,
					mostRecentFile);

			InputStream is = HdfsUtil.openFile(yarnConfiguration,
					mostRecentFile);
			BufferedInputStream bis = new BufferedInputStream(is);

			SolverData<?> solverData = objectMapper.readValue(bis,
					SolverData.class);

			saveLoadInformation(solverId, fileLoadConfiguration, solverData,
					mostRecentFile);

			return solverData;
		} catch (IOException e) {
			throw new PersistenceLoadException(
					"Could not load solver data from path: " + mostRecentFile,
					e);
		}
	}

	private void saveLoadInformation(SolverId solverId,
			FileLoadConfiguration fileLoadConfiguration,
			SolverData<?> solverData, String mostRecentFile)
			throws PersistenceLoadException {
		String path = fileLoadConfiguration.getPath();

		String savePath = FileHandlerUtils.getSavePath(solverId, path);

		String fullPath = savePath + ".startupLoadingInfo";

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			FileLoadInformation fileLoadInformation = new FileLoadInformation(
					"Continue work with existing data", mostRecentFile,
					solverData);
			objectMapper.writeValue(bos, fileLoadInformation);
		} catch (IOException e) {
			throw new PersistenceLoadException(
					"Could not save startupLoadingInfo to file: " + fullPath, e);
		}
	}

	private FileLoadConfiguration getFileLoadConfiguration()
			throws HandlerInitException {
		try {
			return HandlerBuilder.getHandlerConfiguration(solverId,
					FileLoadConfiguration.class);
		} catch (UnknownHandlerException e) {
			LOG.error(
					"Could not get handler configuration for solver {} and handler {}",
					solverId, FileLoadConfiguration.class);
			throw new HandlerInitException(e);
		}
	}

}
