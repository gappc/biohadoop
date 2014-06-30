package at.ac.uibk.dps.biohadoop.solver.moead.master.socket;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.solver.moead.algorithm.Moead;

public class MoeadSocket extends SocketServer {

	@Override
	public String getQueueName() {
		return Moead.MOEAD_QUEUE;
	}

	@Override
	public String getPrefix() {
		return "MOEAD";
	}

	@Override
	public Object getRegistrationObject() {
		return null;
	}
}
