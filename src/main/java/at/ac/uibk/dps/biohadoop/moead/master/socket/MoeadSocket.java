package at.ac.uibk.dps.biohadoop.moead.master.socket;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.moead.master.MoeadEndpointConfig;

public class MoeadSocket extends SocketServer {

	public MoeadSocket() {
		masterConfiguration = new MoeadEndpointConfig();
	}
}
