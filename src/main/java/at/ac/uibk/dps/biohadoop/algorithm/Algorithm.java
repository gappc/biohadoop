package at.ac.uibk.dps.biohadoop.algorithm;

import at.ac.uibk.dps.biohadoop.solver.SolverId;

public interface Algorithm<T> {

	public void compute(SolverId solverId, T parameter) throws AlgorithmException;
	
}
