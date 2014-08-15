package at.ac.uibk.dps.biohadoop.handler.persistence.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileLoader {

	public static final String FILE_LOAD_PATH = "FILE_LOAD_PATH";
	public static final String FILE_LOAD_ON_STARTUP = "FILE_LOAD_ON_STARTUP";

	private static final Logger LOG = LoggerFactory.getLogger(FileLoader.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static SolverData<?> load(SolverId solverId,
			SolverConfiguration solverConfiguration)
			throws FileLoadException {

		String path = solverConfiguration.getProperties().get(FILE_LOAD_PATH);
		if (path == null) {
			throw new FileLoadException("Value for property " + FILE_LOAD_PATH
					+ " not declared");
		}

		String onStartup = solverConfiguration.getProperties().get(
				FILE_LOAD_ON_STARTUP);
		boolean isOnStartup = Boolean.parseBoolean(onStartup);

		if (isOnStartup) {
			LOG.info("Loading data for solver {}", solverId);
			return load(solverId, path);
//			DataClient dataClient = new DataClientImpl(solverId);
//			dataClient.setData(DataOptions.COMPUTATION_RESUMED, true);
//			dataClient.setData(DataOptions.DATA, solverData.getData());
//			dataClient.setData(DataOptions.FITNESS, solverData.getFitness());
//			dataClient.setData(DataOptions.ITERATION_START,
//					solverData.getIteration());
//			dataClient
//					.setData(DataOptions.TIMESTAMP, solverData.getTimestamp());
//			dataClient.setData(DataOptions.TIMEZONE, solverData.getTimezone());
//			LOG.info("Successful loading data for solver {}", solverId);
		}
		return null;
	}

	private static SolverData<?> load(SolverId solverId, String path)
			throws FileLoadException {
		String mostRecentFile = path;

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.exists(yarnConfiguration, path)) {
				throw new FileLoadException("File/Path does not exist: " + path);
			}
			if (HdfsUtil.isDirectory(yarnConfiguration, path)) {
				mostRecentFile = HdfsUtil.getMostRecentFileInPath(
						yarnConfiguration, path);
			}
			if (!HdfsUtil.exists(yarnConfiguration, mostRecentFile)) {
				throw new FileLoadException("Found no file to load data in: "
						+ path);
			}

			LOG.info("Loading data for solver {} from {}", solverId,
					mostRecentFile);

			InputStream is = HdfsUtil.openFile(yarnConfiguration,
					mostRecentFile);
			BufferedInputStream bis = new BufferedInputStream(is);

			SolverData<?> solverData = OBJECT_MAPPER.readValue(bis,
					SolverData.class);

			saveLoadInformation(solverId, path, solverData, mostRecentFile);

			return solverData;
		} catch (IOException e) {
			throw new FileLoadException(
					"Could not load solver data from path: " + mostRecentFile,
					e);
		}
	}

	private static void saveLoadInformation(SolverId solverId, String path,
			SolverData<?> solverData, String mostRecentFile)
			throws FileLoadException {
		String savePath = FileHandlerUtils.getSavePath(solverId, path);

		String fullPath = savePath + ".startupLoadingInfo";

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			FileLoadInformation fileLoadInformation = new FileLoadInformation(
					"Continue work with existing data", mostRecentFile,
					solverData);
			OBJECT_MAPPER.writeValue(bos, fileLoadInformation);
		} catch (IOException e) {
			throw new FileLoadException(
					"Could not save startupLoadingInfo to file: " + fullPath, e);
		}
	}

}
