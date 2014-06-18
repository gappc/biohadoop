package at.ac.uibk.dps.biohadoop.service.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverHandler;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;

public class PersistenceService implements SolverHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(PersistenceService.class);

	private static PersistenceService PERSISTENCE_SERVICE = new PersistenceService();

	private PersistenceService() {
	}

	public static PersistenceService getInstance() {
		return PersistenceService.PERSISTENCE_SERVICE;
	}

	public void onNew(SolverId solverId) {
		LOG.debug("onNew for solver {}", solverId);

		SolverService solverService = SolverService
				.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);

		PersistenceConfiguration persistenceConfiguration = solverConfiguration
				.getPersistenceConfiguration();
		boolean onStartup = persistenceConfiguration.loadConfiguration()
				.isOnStartup();

		if (onStartup) {
			LOG.info(
					"Start loading data for solver with name {} and solverId {}",
					solverConfiguration.getName(), solverId);
			PersistenceController persistenceController = persistenceConfiguration
					.getPersistenceController();
			try {
				SolverData<?> solverData = persistenceController
						.load(solverId);
				solverService.setSolverData(solverId,
						solverData);
				LOG.info(
						"Successful loading data for solver with name {} and solverId {}",
						solverConfiguration.getName(), solverId);
			} catch (PersistenceLoadException e) {
				LOG.error(
						"Error while trying to load startup data for solver {}, message: {}",
						solverId, e);
				// TODO decide what to do in case of load error
				// a) abort
				// b) just print message
				// c) ...?
			}
		}
	}

	@Override
	public void onDataUpdate(SolverId solverId) {
		LOG.debug("onDataUpdate for solver {}", solverId);

		SolverService solverService = SolverService
				.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);
		SolverData<?> solverData = solverService
				.getSolverData(solverId);

		PersistenceConfiguration persistenceConfiguration = solverConfiguration
				.getPersistenceConfiguration();
		int saveAfterEveryIteration = persistenceConfiguration
				.saveConfiguration().getAfterIterations();

		if (solverData.getIteration() % saveAfterEveryIteration == 0) {
			LOG.info(
					"Persisting data for solver with name {} and solverId {}",
					solverConfiguration.getName(), solverId);
			PersistenceController persistenceController = persistenceConfiguration
					.getPersistenceController();
			try {
				persistenceController.save(solverId);
			} catch (PersistenceSaveException e) {
				LOG.error(
						"Error while trying to persist data for solver {}",
						solverId, e);
				// TODO decide what to do in case of save error
				// a) abort
				// b) just print message
				// c) ...?
			}
		}
	}
}
