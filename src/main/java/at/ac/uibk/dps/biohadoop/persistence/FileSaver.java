package at.ac.uibk.dps.biohadoop.persistence;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverData;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;

public class FileSaver {

	public static final String FILE_SAVE_PATH = "FILE_SAVE_PATH";
	public static final String FILE_SAVE_AFTER_ITERATION = "FILE_SAVE_AFTER_ITERATION";

	private static final Logger LOG = LoggerFactory.getLogger(FileSaver.class);

	public static void saveRollingJson(SolverId solverId, Map<String, String> properties,
			SolverData<?> solverData) throws FileSaveException {

		String path = properties.get(FILE_SAVE_PATH);
		if (path == null) {
			throw new FileSaveException("Value for property " + FILE_SAVE_PATH
					+ " not declared");
		}

		FileSaveUtils.saveRolling(solverId, path, solverData);
	}
	
	public static void saveJson(String filename, Object data) throws FileSaveException {
		if (filename == null) {
			throw new FileSaveException("Filename not set");
		}

		FileSaveUtils.saveJson(filename, data);
	}



}
