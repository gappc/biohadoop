package at.ac.uibk.dps.biohadoop.persistence;

import java.util.Map;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;

public class FileSaver {

	public static final String FILE_SAVE_PATH = "FILE_SAVE_PATH";
	public static final String FILE_SAVE_AFTER_ITERATION = "FILE_SAVE_AFTER_ITERATION";

	public static <T>void saveRollingJson(AlgorithmId algorithmId, Map<String, String> properties,
			T data) throws FileSaveException {

		String path = properties.get(FILE_SAVE_PATH);
		if (path == null) {
			throw new FileSaveException("Value for property " + FILE_SAVE_PATH
					+ " not declared");
		}

		FileSaveUtils.saveRolling(algorithmId, path, data);
	}
	
	public static void saveJson(String filename, Object data) throws FileSaveException {
		if (filename == null) {
			throw new FileSaveException("Filename not set");
		}

		FileSaveUtils.saveJson(filename, data);
	}



}
