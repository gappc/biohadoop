package at.ac.uibk.dps.biohadoop.deletable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerInitException;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class PersistenceService2 implements Handler {

	private static final Logger LOG = LoggerFactory
			.getLogger(PersistenceService2.class);

	private static PersistenceService2 PERSISTENCE_SERVICE = new PersistenceService2();

	private PersistenceService2() {
	}

	public static PersistenceService2 getInstance() {
		return PersistenceService2.PERSISTENCE_SERVICE;
	}

	@Override
	public void init(SolverId solverId) throws HandlerInitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(String operation) {
		// TODO Auto-generated method stub
		
	}

//	public void onState(SolverState state, SolverId solverId) {
//		if (state == SolverState.NEW) {
//			LOG.debug("onNew for solver {}", solverId);
//	
//			SolverService solverService = SolverService
//					.getInstance();
//			SolverConfiguration solverConfiguration = solverService
//					.getSolverConfiguration(solverId);
//	
////			PersistenceConfiguration persistenceConfiguration = solverConfiguration
////					.getPersistenceConfiguration();
//
//			PersistenceConfiguration persistenceConfiguration = null;
//			for (HandlerConfiguration handlerConfiguration : solverConfiguration
//					.getHandlerConfigurations()) {
//				if (handlerConfiguration instanceof PersistenceConfiguration) {
//					persistenceConfiguration = (PersistenceConfiguration) handlerConfiguration;
//				}
//			}
//			
//			boolean onStartup = persistenceConfiguration.loadConfiguration()
//					.isOnStartup();
//	
//			if (onStartup) {
//				LOG.info(
//						"Start loading data for solver with name {} and solverId {}",
//						solverConfiguration.getName(), solverId);
//				PersistenceController persistenceController = persistenceConfiguration
//						.getPersistenceController();
//				try {
//					SolverData<?> solverData = persistenceController
//							.load(solverId);
//					solverService.setSolverData(solverId,
//							solverData);
//					LOG.info(
//							"Successful loading data for solver with name {} and solverId {}",
//							solverConfiguration.getName(), solverId);
//				} catch (PersistenceLoadException e) {
//					LOG.error(
//							"Error while trying to load startup data for solver {}, message: {}",
//							solverId, e);
//					// TODO decide what to do in case of load error
//					// a) abort
//					// b) just print message
//					// c) ...?
//				}
//			}
//		}
//	}

//	@Override
//	public void onDataUpdate(SolverId solverId) {
//		LOG.debug("onDataUpdate for solver {}", solverId);
//
//		SolverService solverService = SolverService
//				.getInstance();
//		SolverConfiguration solverConfiguration = solverService
//				.getSolverConfiguration(solverId);
//		SolverData<?> solverData = solverService
//				.getSolverData(solverId);
//
////		PersistenceConfiguration persistenceConfiguration = solverConfiguration
////				.getPersistenceConfiguration();
//
//		PersistenceConfiguration persistenceConfiguration = null;
//		for (HandlerConfiguration handlerConfiguration : solverConfiguration
//				.getHandlerConfigurations()) {
//			if (handlerConfiguration instanceof PersistenceConfiguration) {
//				persistenceConfiguration = (PersistenceConfiguration) handlerConfiguration;
//			}
//		}
//		
//		int saveAfterEveryIteration = persistenceConfiguration
//				.saveConfiguration().getAfterIterations();
//
//		if (solverData.getIteration() % saveAfterEveryIteration == 0) {
//			LOG.info(
//					"Persisting data for solver with name {} and solverId {}",
//					solverConfiguration.getName(), solverId);
//			PersistenceController persistenceController = persistenceConfiguration
//					.getPersistenceController();
//			try {
//				persistenceController.save(solverId);
//			} catch (PersistenceSaveException e) {
//				LOG.error(
//						"Error while trying to persist data for solver {}",
//						solverId, e);
//				// TODO decide what to do in case of save error
//				// a) abort
//				// b) just print message
//				// c) ...?
//			}
//		}
//	}
}
