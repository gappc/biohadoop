package at.ac.uibk.dps.biohadoop.persistence;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

public class FileSaveUtils {

	private static final Logger LOG = LoggerFactory
			.getLogger(FileSaveUtils.class);

	private static final YarnConfiguration YARN_CONFIGURATION = new YarnConfiguration();

	public static <T>void saveRolling(AlgorithmId algorithmId, String path,
			T data) throws FileSaveException {
		String savePath = FileHandlerUtils.getSavePath(algorithmId, path);

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

			String fullPath = savePath + "/" + algorithmId.toString() + "_"
					+ System.currentTimeMillis();

			if (HdfsUtil.exists(yarnConfiguration, fullPath)) {
				throw new FileSaveException("File " + fullPath
						+ " already exists");
			}

			LOG.info("Persisting data for algorithm {} to {}", algorithmId, fullPath);

			OutputStream os = HdfsUtil.createFile(yarnConfiguration, fullPath);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			JsonMapper.OBJECT_MAPPER.writeValue(bos, data);
		} catch (IOException e) {
			throw new FileSaveException("Could not save algorithm " + algorithmId
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
