package at.ac.uibk.dps.biohadoop.service.solver;


public class Solver {

	private final SolverConfiguration solverConfig;
	private int iterationStart;
	private float progress;
	private SolverState solverState = SolverState.NEW;
	private SolverData<?> solverData;
//	private List<SolverHandler> solverHandlers = new ArrayList<>();

	public Solver(SolverConfiguration solverConfig) {
		this.solverConfig = solverConfig;
	}

	public SolverConfiguration getSolverConfiguration() {
		return solverConfig;
	}

	public int getResumeIterationIndex() {
		return iterationStart;
	}

	public void setResumeIterationIndex(int iterationStart) {
		this.iterationStart = iterationStart;
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

	public SolverData<?> getSolverData() {
		return solverData;
	}

	public <T> void setSolverData(SolverData<T> solverData) {
		this.solverData = solverData;
	}

//	public void registerSolverHandler(
//			final SolverHandler solverHandler) {
//		solverHandlers.add(solverHandler);
//	}
//
//	public List<SolverHandler> getSolverHandlers() {
//		return solverHandlers;
//	}

}
