package at.ac.uibk.dps.biohadoop.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.solver.SolverService;

public class HandlerBuilder {

	private static final Logger LOG = LoggerFactory
			.getLogger(HandlerBuilder.class);

	private HandlerBuilder() {
	}
	
	public static Handler getHandler(SolverId solverId,
			Class<? extends HandlerConfiguration> handlerConfigurationType)
			throws UnknownHandlerException {

		HandlerConfiguration handlerConfiguration = HandlerBuilder
				.getHandlerConfiguration(solverId, handlerConfigurationType);

		Class<? extends Handler> handlerClass = handlerConfiguration
				.getHandler();
		try {
			return handlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			LOG.error(
					"Could not instanciate handler {} for configuration {}",
					handlerClass, handlerConfigurationType, e);
			throw new UnknownHandlerException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends HandlerConfiguration>T getHandlerConfiguration(
			SolverId solverId,
			Class<T> handlerConfigurationType)
			throws UnknownHandlerException {
		SolverService solverService = SolverService.getInstance();
		SolverConfiguration solverConfiguration = solverService
				.getSolverConfiguration(solverId);

		for (HandlerConfiguration handlerConfiguration : solverConfiguration
				.getHandlerConfigurations()) {
			if (handlerConfiguration.getClass() == handlerConfigurationType) {
				LOG.info("Found configuration {}", handlerConfigurationType);
				return (T)handlerConfiguration;
			}
		}

		throw new UnknownHandlerException();
	}
}
