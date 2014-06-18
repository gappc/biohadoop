package at.ac.uibk.dps.biohadoop.service.solver;

public interface SolverHandler {

	public void onNew(SolverId solverId);
	public void onDataUpdate(SolverId solverId);

}
