package at.ac.uibk.dps.biohadoop.service.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.torename.ObjectCloner;

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

	public float getOverallProgress() {
		float progress = 0;
		int solverCount = solvers.size();
		for (SolverId solverId : solvers.keySet()) {
			Solver solver = solvers.get(solverId);
			progress += solver.getProgress() / solverCount;
		}
		return progress > 1 ? 1 : progress;
	}

	public void setProgress(final SolverId solverId, final float progress) {
		Solver solver = solvers.get(solverId);
		solver.setProgress(progress);
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
			for (SolverHandler solverHandler : solver.getSolverHandlers()) {
				solverHandler.onNew(solverId);
			}
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

	public void setSolverData(final SolverId solverId,
			final SolverData<?> solverData) {
		Solver solver = solvers.get(solverId);
		SolverData<?> clone = ObjectCloner.deepCopy(solverData,
				SolverData.class);
		solver.setSolverData(clone);
		for (SolverHandler solverHandler : solver.getSolverHandlers()) {
			solverHandler.onDataUpdate(solverId);
		}
	}

	public void updateSolverData(final SolverId solverId,
			final SolverData<?> solverData) {
		Solver solver = solvers.get(solverId);
		SolverData<?> clone = ObjectCloner.deepCopy(solverData,
				SolverData.class);
		solver.setSolverData(clone);
	}

	public SolverData<?> getSolverData(SolverId solverId) {
		Solver solver = solvers.get(solverId);
		if (solver == null) {
			return null;
		}
		return solver.getSolverData();
	}

	// TODO remove if only needed for DistributionService.getRemoteSolver()
	public List<SolverId> getSolversList() {
		return new ArrayList<>(solvers.keySet());
	}

	public SolverConfiguration getSolverConfiguration(SolverId solverId) {
		Solver solver = solvers.get(solverId);
		return solver.getSolverConfiguration();
	}

}
