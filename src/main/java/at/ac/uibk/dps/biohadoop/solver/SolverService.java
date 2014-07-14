package at.ac.uibk.dps.biohadoop.solver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolverService {

	private static final Logger LOG = LoggerFactory
			.getLogger(SolverService.class);

	private static final SolverService SOLVER_MANAGER = new SolverService();
	private Map<SolverId, Solver> solvers = new ConcurrentHashMap<>();
	private AtomicInteger counter = new AtomicInteger();

	private SolverService() {
	}

	public static SolverService getInstance() {
		return SolverService.SOLVER_MANAGER;
	}

	public SolverId addSolver(final Solver solver) {
		SolverId solverId = SolverId.newInstance();
		solvers.put(solverId, solver);
		counter.incrementAndGet();
		return solverId;
	}

	public float getProgress(final SolverId solverId) {
		Solver solver = solvers.get(solverId);
		return solver.getProgress();
	}

	public void setProgress(final SolverId solverId, final float progress) {
		Solver solver = solvers.get(solverId);
		solver.setProgress(progress);
	}
	
	public float getOverallProgress() {
		float progress = 0;
		int solverCount = solvers.size();
		for (SolverId solverId : solvers.keySet()) {
			Solver solver = solvers.get(solverId);
			progress += solver.getProgress() / solverCount;
		}
		return progress > 1 ? 1 : progress;
	}

	public SolverState getSolverState(final SolverId solverId) {
		Solver solver = solvers.get(solverId);
		return solver.getSolverState();
	}

	public void setSolverState(final SolverId solverId, SolverState solverState) {
		Solver solver = solvers.get(solverId);
		solver.setSolverState(solverState);

		switch (solverState) {
		case NEW:
			break;
		case RUNNING:
			break;
		case FINISHED:
			counter.decrementAndGet();
			if (counter.compareAndSet(0, 0)) {
				LOG.info("All algorithms terminated");
			}
			break;
		default:
			break;
		}
	}

	public SolverConfiguration getSolverConfiguration(SolverId solverId) {
		Solver solver = solvers.get(solverId);
		return solver.getSolverConfiguration();
	}

}
