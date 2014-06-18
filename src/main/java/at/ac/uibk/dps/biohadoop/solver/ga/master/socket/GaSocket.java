package at.ac.uibk.dps.biohadoop.solver.ga.master.socket;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaEndpointConfig;

public class GaSocket extends SocketServer {

	public GaSocket() {
		masterConfiguration = new GaEndpointConfig();
	}
}
