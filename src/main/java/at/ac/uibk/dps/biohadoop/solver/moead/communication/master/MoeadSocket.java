package at.ac.uibk.dps.biohadoop.solver.moead.communication.master;

import at.ac.uibk.dps.biohadoop.communication.master.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;

public class MoeadSocket extends SocketServer {

	@Override
	public String getQueueName() {
		return Moead.MOEAD_QUEUE;
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}
}
