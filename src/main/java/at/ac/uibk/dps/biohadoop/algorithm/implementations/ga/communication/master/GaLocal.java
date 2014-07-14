package at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.worker.LocalGaWorker;
import at.ac.uibk.dps.biohadoop.communication.master.local.LocalEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;

public class GaLocal extends LocalEndpoint {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistances();
	}

	@Override
	public Class<? extends LocalWorker<?, ?>> getWorkerClass() {
		return LocalGaWorker.class;
	}

}
