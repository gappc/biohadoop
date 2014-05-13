package at.ac.uibk.dps.biohadoop.torename;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HdfsUtil {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HdfsUtil.class);

	public static boolean fileExists(YarnConfiguration conf, String filename) {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		try {
			FileSystem fs = FileSystem.get(conf);
			boolean exists = fs.exists(path);
			if (!exists) {
				LOGGER.error(
						"Could not find file, fs.defaultFS={}, path={}",
						defaultFs, filename);
			}
			return exists;
		} catch (IOException e) {
			LOGGER.error(
					"Could not find file, fs.defaultFS={}, path={}",
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
	
	public static OutputStream createFile(YarnConfiguration conf, String filename)
			throws IOException {
		String defaultFs = conf.get("fs.defaultFS");
		Path path = new Path(defaultFs + filename);
		FileSystem fs = FileSystem.get(conf);
		return fs.create(path);
	}

}
