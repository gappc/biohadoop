package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.ga.worker.SocketGaWorker;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.nsgaii.algorithm.NsgaII;

public class NsgaIISocket {

	private static final String ENVIRONMENT_PREFIX = "NSGAII";

	public void startMaster() {
		SocketServer socketServer = new SocketServer();
//		socketServer.startEndpoints(GaMasterImpl.class, ENVIRONMENT_PREFIX,
//				NsgaII.NSGAII_QUEUE);
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