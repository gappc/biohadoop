package at.ac.uibk.dps.biohadoop.solver.ga.master.kryo;

import at.ac.uibk.dps.biohadoop.connection.kryo.KryoServer;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.algorithm.Ga;

public class GaKryo extends KryoServer {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public String getPrefix() {
		return "GA";
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistancesAsObject();
	}
}
