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
import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerClientImpl;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;
import at.ac.uibk.dps.biohadoop.handler.HandlerConstants;
import at.ac.uibk.dps.biohadoop.handler.HandlerService;
import at.ac.uibk.dps.biohadoop.handler.progress.ProgressHandler;
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
				.getSolverConfiguration()) {
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

		registerHandlers(solverId, solverConfig);

		return generateCallable(solverId, solverConfig);
	}

	private static void registerHandlers(SolverId solverId,
			SolverConfiguration solverConfig) throws SolverLaunchException {
		HandlerService handlerService = HandlerService.getInstance();

		// Register default handlers
		handlerService.registerHandler(solverId, new ProgressHandler());

		try {
			if (solverConfig.getHandlerConfigurations() != null) {
				// Register dynamic handlers
				for (HandlerConfiguration handlerConfiguration : solverConfig
						.getHandlerConfigurations()) {
					Handler handler = handlerConfiguration.getHandler()
							.newInstance();
					handlerService.registerHandler(solverId, handler);
				}
			}
			for (Handler handler : handlerService.getHandlers(solverId)) {
				handler.init(solverId);
			}
		} catch (Exception e) {
			LOG.error("Could not register handlers", e);
			throw new SolverLaunchException(e);

		}
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

				HandlerClientImpl handlerClientImpl = new HandlerClientImpl(
						solverId);
				handlerClientImpl.invokeHandlers(
						HandlerConstants.ALGORITHM_START, null);

				Object parameter = solverConfig.getAlgorithmConfiguration();
				Algorithm<Object> algorithm = (Algorithm<Object>) solverConfig
						.getAlgorithm().newInstance();

				SolverService.getInstance().setSolverState(solverId,
						SolverState.RUNNING);

				algorithm.compute(solverId, parameter);

				handlerClientImpl.invokeHandlers(
						HandlerConstants.ALGORITHM_STOP, null);

				SolverService.getInstance().setSolverState(solverId,
						SolverState.SUCCEEDED);

				LOG.info("Finished solver {} with solverId {}",
						solverConfig.getName(), solverId);
				return solverId;
			}
		};
	}
}
