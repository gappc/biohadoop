package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerConstants;
import at.ac.uibk.dps.biohadoop.handler.HandlerInitException;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public class IterationStepHandler2 implements Handler {

	private SolverId solverId;

	@Override
	public void init(SolverId solverId) throws HandlerInitException {
		this.solverId = solverId;
	}

	@Override
	public void update(String operation) {
		if (HandlerConstants.DEFAULT.equals(operation)) {
//			SolverData<?> solverData = (SolverData<?>) data;
//			SolverService.getInstance().setSolverData(solverId, solverData);
		}
	}

}
