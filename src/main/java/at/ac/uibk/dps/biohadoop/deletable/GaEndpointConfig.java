package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointHandler;

public class GaEndpointConfig {// implements MasterConfiguration {

	private final static String PREFIX = "GA";

//	@Override
//	public Class<? extends MasterEndpoint> getMasterEndpoint() {
//		return GaMasterImpl.class;
//	}
	
	public String getPrefix() {
		return PREFIX;
	}

	public String getQueueName() {
		return PREFIX + "_QUEUE";
	}

}
