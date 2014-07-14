package at.ac.uibk.dps.biohadoop.solver;


public class Solver {

	private final SolverConfiguration solverConfig;
	private float progress;
	private SolverState solverState = SolverState.NEW;

	public Solver(SolverConfiguration solverConfig) {
		this.solverConfig = solverConfig;
	}

	public SolverConfiguration getSolverConfiguration() {
		return solverConfig;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public SolverState getSolverState() {
		return solverState;
	}

	public void setSolverState(SolverState solverState) {
		this.solverState = solverState;
	}

}
