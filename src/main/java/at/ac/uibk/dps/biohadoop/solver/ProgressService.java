package at.ac.uibk.dps.biohadoop.solver;


public class ProgressService {

	private ProgressService() {
	}

	public static void setProgress(SolverId solverId, float progress) {
		SolverService.getInstance().setProgress(solverId, progress);
	}

}
