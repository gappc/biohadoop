package at.ac.uibk.dps.biohadoop.deletable;


public class SolverEndpoint {

	private final SolverType solverType;
	private final ConnectionType connectionType;

	public SolverEndpoint(SolverType solverType,
			ConnectionType connectionType) {
		this.solverType = solverType;
		this.connectionType = connectionType;
	}

	public SolverType getSolverType() {
		return solverType;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}
	
	@Override
	public String toString() {
		return getShort(solverType, connectionType);
	}
	
	public static String getShort(SolverType solverType, ConnectionType connectionType) {
		return solverType + "_" + connectionType;
	}
	
	public static SolverEndpoint buildSolverEndpoint(String solverEndpoint) {
		String[] tokens = solverEndpoint.split("_");
		SolverType solverType = SolverType.valueOf(tokens[0]);
		ConnectionType connectionType = ConnectionType.valueOf(tokens[1]);
		return new SolverEndpoint(solverType, connectionType);
	}
}
