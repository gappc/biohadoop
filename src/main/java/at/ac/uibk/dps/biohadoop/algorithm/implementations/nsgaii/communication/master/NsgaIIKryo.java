package at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.communication.master;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.nsgaii.algorithm.NsgaII;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoServer;

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
