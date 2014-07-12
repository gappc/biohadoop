package at.ac.uibk.dps.biohadoop.handler;

import at.ac.uibk.dps.biohadoop.service.solver.SolverId;

public interface Handler {

	public void init(SolverId solverId) throws HandlerInitException;
//	TODO should throw exeception? what to do if exception is thrown?
	public void update(String operation);

}
