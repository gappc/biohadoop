package at.ac.uibk.dps.biohadoop.ga.master.kryo;

import at.ac.uibk.dps.biohadoop.connection.kryo.KryoServer;
import at.ac.uibk.dps.biohadoop.ga.master.GaEndpointConfig;

public class GaKryo extends KryoServer {

	public GaKryo() {
		masterConfiguration = new GaEndpointConfig();
	}
}
