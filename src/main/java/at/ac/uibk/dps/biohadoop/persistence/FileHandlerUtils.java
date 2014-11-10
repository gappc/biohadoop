package at.ac.uibk.dps.biohadoop.persistence;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;

public class FileHandlerUtils {

	private FileHandlerUtils() {
	}
	
	public static String getSavePath(AlgorithmId algorithmId, String path) {
		String savePath = path;
		if (savePath.charAt(savePath.length() - 1) != '/') {
			savePath += "/";
		}

		savePath += algorithmId + "/";

		return savePath;
	}
	
}
