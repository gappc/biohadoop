package at.ac.uibk.dps.biohadoop.solver.nsgaii.master.socket;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.solver.moead.master.MoeadEndpointConfig;

public class NsgaIISocket extends SocketServer {

	public NsgaIISocket() {
		masterConfiguration = new MoeadEndpointConfig();
	}
}
