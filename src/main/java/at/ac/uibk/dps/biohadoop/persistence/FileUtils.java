package at.ac.uibk.dps.biohadoop.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

public class FileUtils {

	private static final YarnConfiguration YARN_CONFIGURATION = new YarnConfiguration();
	
	public static <T>void save(String filename, T data)
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

	public static <T> T load(String path, Class<T> dataClass)
			throws FileLoadException {
		if (path == null) {
			throw new FileLoadException("Path is null");
		}

		YarnConfiguration yarnConfiguration = new YarnConfiguration();
		try {
			if (!HdfsUtil.exists(yarnConfiguration, path)) {
				throw new FileLoadException("File/Path does not exist: " + path);
			}

			InputStream is = HdfsUtil.openFile(yarnConfiguration, path);
			BufferedInputStream bis = new BufferedInputStream(is);

			return JsonMapper.OBJECT_MAPPER.readValue(bis, dataClass);
		} catch (IOException e) {
			throw new FileLoadException(
					"Could not load algorithm data from path: " + path, e);
		}
	}

}
