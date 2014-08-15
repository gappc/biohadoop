package at.ac.uibk.dps.biohadoop.handler.persistence.file;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileSaver {

	public static final String FILE_SAVE_PATH = "FILE_SAVE_PATH";
	public static final String FILE_SAVE_AFTER_ITERATION = "FILE_SAVE_AFTER_ITERATION";

	private static final Logger LOG = LoggerFactory.getLogger(FileSaver.class);

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static void save(SolverId solverId,
			SolverConfiguration solverConfiguration, SolverData<?> solverData)
			throws FileSaveException {

		String path = solverConfiguration.getProperties().get(FILE_SAVE_PATH);
		if (path == null) {
			throw new FileSaveException("Value for property " + FILE_SAVE_PATH
					+ " not declared");
		}

		save(solverId, path, solverData);
	}

	private static void save(SolverId solverId, String path,
			SolverData<?> solverData) throws FileSaveException {
		String savePath = FileHandlerUtils.getSavePath(solverId, path);

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.isDirectory(yarnConfiguration, savePath)) {
				boolean dirCreated = HdfsUtil
						.mkDir(yarnConfiguration, savePath);
				if (!dirCreated) {
					throw new FileSaveException(
							"Could not create directory with path " + savePath);
				}
			}

			String fullPath = savePath + "/" + solverId.toString() + "_"
					+ solverData.getIteration() + "_"
					+ solverData.getTimestamp();

			if (HdfsUtil.exists(yarnConfiguration, fullPath)) {
				throw new FileSaveException("File " + fullPath
						+ " already exists");
			}

			LOG.info("Persisting data for solver {} to {}", solverId, fullPath);
			
			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			OBJECT_MAPPER.writeValue(bos, solverData);
		} catch (IOException e) {
			throw new FileSaveException("Could not save solver " + solverId
					+ " to file: " + savePath, e);
		}
	}

}
