package at.ac.uibk.dps.biohadoop.service.persistence.file;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class FileHandlerUtils {

	private FileHandlerUtils() {
	}
	
	public static String getSavePath(SolverId solverId, String path) {
		String savePath = path;
		if (savePath.charAt(savePath.length() - 1) != '/') {
			savePath += "/";
		}

		savePath += solverId + "/";

		return savePath;
	}
	
}
