package at.ac.uibk.dps.biohadoop.service.persistence;

import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public interface PersistenceController {

	public SolverData<?> load(SolverId solverId) throws PersistenceLoadException;
	public void save(SolverId solverId) throws PersistenceSaveException;
}
