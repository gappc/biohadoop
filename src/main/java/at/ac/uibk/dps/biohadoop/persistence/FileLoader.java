package at.ac.uibk.dps.biohadoop.persistence;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

public class FileLoader {

	public static final String FILE_LOAD_PATH = "FILE_LOAD_PATH";
	public static final String FILE_LOAD_ON_STARTUP = "FILE_LOAD_ON_STARTUP";

	private static final Logger LOG = LoggerFactory.getLogger(FileLoader.class);

	public static <T>T load(Map<String, String> properties) throws FileLoadException {

		String path = properties.get(FILE_LOAD_PATH);
		if (path == null) {
			throw new FileLoadException("Value for property " + FILE_LOAD_PATH
					+ " not declared");
		}

		String onStartup = properties.get(FILE_LOAD_ON_STARTUP);
		boolean isOnStartup = Boolean.parseBoolean(onStartup);

		if (isOnStartup) {
			return load(path);
		}
		return null;
	}

	private static <T>T load(String path)
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

			LOG.info("Loading data from {}", mostRecentFile);

			InputStream is = HdfsUtil.openFile(yarnConfiguration,
					mostRecentFile);
			BufferedInputStream bis = new BufferedInputStream(is);

			return JsonMapper.OBJECT_MAPPER.readValue(bis,
					new TypeReference<T>() {});
		} catch (IOException e) {
			throw new FileLoadException(
					"Could not load algorithm data from path: " + mostRecentFile,
					e);
		}
	}

}
