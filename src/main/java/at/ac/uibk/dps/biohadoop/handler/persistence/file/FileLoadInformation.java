package at.ac.uibk.dps.biohadoop.handler.persistence.file;

import at.ac.uibk.dps.biohadoop.service.solver.SolverData;

public class FileLoadInformation {

	private final String message;
	private final String loadedFrom;
	private final SolverData<?> solverData;

	public FileLoadInformation(String message, String loadedFrom,
			SolverData<?> solverData) {
		this.message = message;
		this.loadedFrom = loadedFrom;
		this.solverData = solverData;
	}

	public String getLoadedFrom() {
		return loadedFrom;
	}

	public String getMessage() {
		return message;
	}

	public SolverData<?> getSolverData() {
		return solverData;
	}
}
