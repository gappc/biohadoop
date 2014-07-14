package at.ac.uibk.dps.biohadoop.solver.moead.communication.master;

import at.ac.uibk.dps.biohadoop.communication.master.local.LocalEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.solver.moead.communication.worker.LocalMoeadWorker;

public class MoeadLocal extends LocalEndpoint {

	@Override
	public String getQueueName() {
		return Moead.MOEAD_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

	@Override
	public Class<? extends LocalWorker<?, ?>> getWorkerClass() {
		return LocalMoeadWorker.class;
	}
}
