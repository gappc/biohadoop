package at.ac.uibk.dps.biohadoop.solver.moead.master;

import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.torename.MasterConfiguration;

public class MoeadEndpointConfig implements MasterConfiguration {

	private final static String PREFIX = "MOEAD";

	@Override
	public Class<? extends MasterEndpoint> getMasterEndpoint() {
		return MoeadMasterImpl.class;
	}
	
	public String getPrefix() {
		return PREFIX;
	}

	public String getQueueName() {
		return PREFIX + "_QUEUE";
	}

}
