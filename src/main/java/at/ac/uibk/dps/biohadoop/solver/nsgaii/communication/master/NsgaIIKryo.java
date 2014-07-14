package at.ac.uibk.dps.biohadoop.solver.nsgaii.communication.master;

import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoServer;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.NsgaII;

public class NsgaIIKryo extends KryoServer {

	@Override
	public String getQueueName() {
		return NsgaII.NSGAII_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

}
