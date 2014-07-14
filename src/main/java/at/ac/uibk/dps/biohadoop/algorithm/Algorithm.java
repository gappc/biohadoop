package at.ac.uibk.dps.biohadoop.algorithm;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public interface Algorithm<T, S> {

	public S compute(SolverId solverId, T parameter) throws AlgorithmException;
	
}
