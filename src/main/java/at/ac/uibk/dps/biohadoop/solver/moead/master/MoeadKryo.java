package at.ac.uibk.dps.biohadoop.solver.moead.master;

import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoServer;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;

public class MoeadKryo extends KryoServer {

	@Override
	public String getQueueName() {
		return Moead.MOEAD_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}

}
