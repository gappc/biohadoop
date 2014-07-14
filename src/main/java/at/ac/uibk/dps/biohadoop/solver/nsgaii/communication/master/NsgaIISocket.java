package at.ac.uibk.dps.biohadoop.solver.nsgaii.communication.master;

import at.ac.uibk.dps.biohadoop.communication.master.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm.NsgaII;

public class NsgaIISocket extends SocketServer {

	@Override
	public String getQueueName() {
		return NsgaII.NSGAII_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}
}
