package at.ac.uibk.dps.biohadoop.persistence;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

public class FileSaveUtils {

	private static final Logger LOG = LoggerFactory
			.getLogger(FileSaveUtils.class);

	private static final YarnConfiguration YARN_CONFIGURATION = new YarnConfiguration();

	public static void saveRolling(SolverId solverId, String path,
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
			JsonMapper.OBJECT_MAPPER.writeValue(bos, solverData);
		} catch (IOException e) {
			throw new FileSaveException("Could not save solver " + solverId
					+ " to file: " + savePath, e);
		}
	}

	public static void saveJson(String filename, Object data)
			throws FileSaveException {
		try {
			OutputStream os = HdfsUtil.createFile(YARN_CONFIGURATION, filename);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			JsonMapper.OBJECT_MAPPER.writeValue(bos, data);
		} catch (IOException e) {
			throw new FileSaveException("Could not save data to file: "
					+ filename, e);
		}
	}
}
