package at.ac.uibk.dps.biohadoop.torename;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsUtil {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(HdfsUtil.class);

	public static boolean exists(YarnConfiguration conf, String filename) {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		try {
			FileSystem fs = FileSystem.get(conf);
			boolean exists = fs.exists(path);
			if (!exists) {
				LOGGER.debug("Could not find file, fs.defaultFS={}, path={}",
						defaultFs, filename);
			}
			return exists;
		} catch (IOException e) {
			LOGGER.error("Could not find file, fs.defaultFS={}, path={}",
					defaultFs, filename);
			return false;
		}
	}

	public static InputStream openFile(YarnConfiguration conf, String filename)
			throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		FileSystem fs = FileSystem.get(conf);
		return fs.open(path);
	}

	public static OutputStream createFile(YarnConfiguration conf,
			String filename) throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		FileSystem fs = FileSystem.get(conf);
		return fs.create(path);
	}

	public static boolean isDirectory(YarnConfiguration conf, String filename)
			throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		return FileSystem.get(conf).isDirectory(path);
	}

	public static boolean isFile(YarnConfiguration conf, String filename)
			throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		return FileSystem.get(conf).isFile(path);
	}

	public static boolean mkDir(YarnConfiguration conf, String pathname)
			throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + pathname);
		FileSystem fs = FileSystem.get(conf);
		return FileSystem.mkdirs(fs, path, FsPermission.getDirDefault());
	}

	public static String getMostRecentFileInPath(YarnConfiguration conf,
			String pathname) throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + pathname);

		Path latestPath = null;
		long latestModificationTime = 0;
		FileStatus[] fileStates = FileSystem.get(conf).listStatus(path);
		for (FileStatus fileStatus : fileStates) {
			if (fileStatus.getModificationTime() > latestModificationTime) {
				latestPath = fileStatus.getPath();
				latestModificationTime = fileStatus.getModificationTime();
			}
		}
		if (latestPath == null) {
			return null;
		}
		return Path.getPathWithoutSchemeAndAuthority(latestPath).toString();
	}
}
