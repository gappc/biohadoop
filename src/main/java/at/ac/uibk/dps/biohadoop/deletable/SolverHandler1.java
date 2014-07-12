package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverState;

public interface SolverHandler1 {

	public void onState(SolverState state, SolverId solverId) throws Exception;
	public void onDataUpdate(SolverId solverId) throws Exception;

}
