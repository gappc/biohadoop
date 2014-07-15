package at.ac.uibk.dps.biohadoop.solver;


public class ProgressClientImpl implements ProgressClient {

	private SolverId solverId;
	
	public ProgressClientImpl(SolverId solverId) {
		this.solverId = solverId;
	}
	
	@Override
	public void setProgress(float progress) {
		SolverService.getInstance().setProgress(solverId, progress);
	}

}
