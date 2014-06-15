package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.ga.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;

public class MoeadSocket {

	private static final String ENVIRONMENT_PREFIX = "MOEAD";

	public void startMaster() {
		SocketServer socketServer = new SocketServer();
//		socketServer.startEndpoints(GaMasterImpl.class, ENVIRONMENT_PREFIX,
//				Moead.MOEAD_QUEUE);
	}

	public String getWorkerParameters() {
		return SocketGaWorker.class.getCanonicalName() + " " + getHost() + " "
				+ getPort();
	}

	public String getHost() {
		return Environment.getPrefixed(ENVIRONMENT_PREFIX,
				Environment.SOCKET_HOST);
	}

	public int getPort() {
		return Integer.valueOf(Environment.getPrefixed(ENVIRONMENT_PREFIX,
				Environment.SOCKET_PORT));
	}

}