package at.ac.uibk.dps.biohadoop.solver.ga.master.kryo;

import at.ac.uibk.dps.biohadoop.connection.kryo.KryoServer;
import at.ac.uibk.dps.biohadoop.solver.ga.master.GaEndpointConfig;

public class GaKryo extends KryoServer {

	public GaKryo() {
		masterConfiguration = new GaEndpointConfig();
	}
}
