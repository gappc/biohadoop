package at.ac.uibk.dps.biohadoop.algorithm;

import at.ac.uibk.dps.biohadoop.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.solver.SolverId;

public interface Algorithm {

	public void compute(SolverId solverId, SolverConfiguration configuration) throws AlgorithmException;
	
}
