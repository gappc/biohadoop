package at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.communication.master;

import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.algorithm.implementations.ga.algorithm.Ga;
import at.ac.uibk.dps.biohadoop.communication.master.socket.SocketServer;

public class GaSocket extends SocketServer {

	@Override
	public String getQueueName() {
		return Ga.GA_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return DistancesGlobal.getDistancesAsObject();
	}
}
