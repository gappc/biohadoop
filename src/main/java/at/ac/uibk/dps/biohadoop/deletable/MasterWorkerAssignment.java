package at.ac.uibk.dps.biohadoop.deletable;

import java.util.HashMap;
import java.util.Map;

import at.ac.uibk.dps.biohadoop.connection.socket.SocketServer;
import at.ac.uibk.dps.biohadoop.ga.master.GaMasterImpl;
import at.ac.uibk.dps.biohadoop.ga.master.local.GaLocalResource;
import at.ac.uibk.dps.biohadoop.moead.master.MoeadMasterImpl;
import at.ac.uibk.dps.biohadoop.nsgaii.master.NsgaIIMasterImpl;

public class MasterWorkerAssignment {
	
	private final static Map<SolverType, Class<?>> SOLVER_IMPLEMENTATIONS;
	private final static Map<ConnectionType, Class<?>> COMMUNICATION_IMPLEMENTATIONS;
	
	static {
		SOLVER_IMPLEMENTATIONS = new HashMap<>();
		SOLVER_IMPLEMENTATIONS.put(SolverType.GA, GaMasterImpl.class);
		SOLVER_IMPLEMENTATIONS.put(SolverType.MOEAD, MoeadMasterImpl.class);
		SOLVER_IMPLEMENTATIONS.put(SolverType.NSGAII, NsgaIIMasterImpl.class);
		
		COMMUNICATION_IMPLEMENTATIONS = new HashMap<>();
		COMMUNICATION_IMPLEMENTATIONS.put(ConnectionType.LOCAL, GaLocalResource.class);
		COMMUNICATION_IMPLEMENTATIONS.put(ConnectionType.REST, GaRestResource.class);
		COMMUNICATION_IMPLEMENTATIONS.put(ConnectionType.SOCKET, SocketServer.class);
		COMMUNICATION_IMPLEMENTATIONS.put(ConnectionType.WEBSOCKET, GaWebSocketResource.class);
	}
	
	private MasterWorkerAssignment() {
	}
	
	public static Map<SolverType, Class<?>> getSolverImplementations() {
		return SOLVER_IMPLEMENTATIONS;
	}

	public static Map<ConnectionType, Class<?>> getCommunicationImplementations() {
		return COMMUNICATION_IMPLEMENTATIONS;
	}
}
