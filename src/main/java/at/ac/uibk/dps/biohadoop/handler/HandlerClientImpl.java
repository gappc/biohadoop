package at.ac.uibk.dps.biohadoop.handler;

import java.util.List;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class HandlerClientImpl implements HandlerClient {

	private final SolverId solverId;
	
	public HandlerClientImpl(SolverId solverId) {
		this.solverId = solverId;
	}
	
	@Override
	public void invokeDefaultHandlers() {
		List<Handler> handlers = HandlerService.getInstance().getHandlers(solverId);
		for (Handler handler : handlers) {
			handler.update(HandlerConstants.DEFAULT);
		}
	}
	
	@Override
	public void invokeHandlers(String operation, Object data) {
		List<Handler> handlers = HandlerService.getInstance().getHandlers(solverId);
		for (Handler handler : handlers) {
			handler.update(operation);
		}
	}
	
}
