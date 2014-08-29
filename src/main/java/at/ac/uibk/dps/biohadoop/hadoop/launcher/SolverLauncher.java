package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.Algorithm;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.solver.Solver;
import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.solver.SolverService;
import at.ac.uibk.dps.biohadoop.solver.SolverState;

public class SolverLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(SolverLauncher.class);

	private SolverLauncher() {
	}

	public static List<Future<SolverId>> launchSolver(
			BiohadoopConfiguration biohadoopConfig)
			throws SolverLaunchException {
		ExecutorService cachedPoolExecutor = Executors.newCachedThreadPool();

		List<Future<SolverId>> solvers = new ArrayList<>();

		for (SolverConfiguration solverConfig : biohadoopConfig
				.getSolverConfigurations()) {
			Callable<SolverId> callable = prepareSolver(solverConfig);
			Future<SolverId> solver = cachedPoolExecutor.submit(callable);
			solvers.add(solver);
		}

		// shutdown() can be called here safely, as it just ignores new threads
		// but completes the old ones
		cachedPoolExecutor.shutdown();

		return solvers;
	}

	private static Callable<SolverId> prepareSolver(
			final SolverConfiguration solverConfig)
			throws SolverLaunchException {
		final Solver solver = new Solver(solverConfig);

		final SolverService solverService = SolverService.getInstance();
		final SolverId solverId = solverService.addSolver(solver);

		return generateCallable(solverId, solverConfig);
	}

	private static Callable<SolverId> generateCallable(final SolverId solverId,
			final SolverConfiguration solverConfig) {
		return new Callable<SolverId>() {

			@Override
			public SolverId call() throws Exception {
				LOG.info("Initialising solver {} with solverId {}",
						solverConfig.getName(), solverId);
				SolverService.getInstance().setSolverState(solverId,
						SolverState.NEW);

				Algorithm algorithm = (Algorithm) solverConfig
						.getAlgorithm().newInstance();

				SolverService.getInstance().setSolverState(solverId,
						SolverState.RUNNING);

				algorithm.compute(solverId, solverConfig.getProperties());

				SolverService.getInstance().setSolverState(solverId,
						SolverState.SUCCEEDED);

				LOG.info("Finished solver {} with solverId {}",
						solverConfig.getName(), solverId);
				return solverId;
			}
		};
	}
}
