package at.ac.uibk.dps.biohadoop.config;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public interface Algorithm<T, S> {

	public T compute(SolverId solverId, S parameter) throws AlgorithmException;
	
}
