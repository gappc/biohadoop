package at.ac.uibk.dps.biohadoop.solver.ga.communication.master;

import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoServer;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

public class GaKryo extends KryoServer {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistancesAsObject();
	}
}
