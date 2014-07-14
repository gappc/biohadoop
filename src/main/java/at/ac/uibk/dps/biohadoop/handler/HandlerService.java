package at.ac.uibk.dps.biohadoop.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.solver.SolverId;

public class HandlerService {

	private static final Logger LOG = LoggerFactory
			.getLogger(HandlerService.class);

	private static final HandlerService HANDLER_SERVICE = new HandlerService();
	private Map<SolverId, List<Handler>> handlersForSolver = new ConcurrentHashMap<>();

	private HandlerService() {
	}

	public static HandlerService getInstance() {
		return HandlerService.HANDLER_SERVICE;
	}

	public synchronized void registerHandler(SolverId solverId,
			Handler handler) {
		List<Handler> handlers = handlersForSolver.get(solverId);
		if (handlers == null) {
			handlers = new CopyOnWriteArrayList<Handler>();
			handlersForSolver.put(solverId, handlers);
		}
		handlers.add(handler);
		LOG.debug("Added handler {} for solver {}", handler, solverId);
	}

	public synchronized List<Handler> getHandlers(SolverId solverId) {
		return handlersForSolver.get(solverId);
	}

}
