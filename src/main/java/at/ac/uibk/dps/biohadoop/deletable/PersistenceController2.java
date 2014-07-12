package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceLoadException;
import at.ac.uibk.dps.biohadoop.service.persistence.PersistenceSaveException;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public interface PersistenceController2 {

	public SolverData<?> load(SolverId solverId) throws PersistenceLoadException;
	public void save(SolverId solverId) throws PersistenceSaveException;
}
