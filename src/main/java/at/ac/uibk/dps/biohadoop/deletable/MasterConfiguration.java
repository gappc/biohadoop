package at.ac.uibk.dps.biohadoop.deletable;

import at.ac.uibk.dps.biohadoop.connection.DefaultEndpointHandler;

public interface MasterConfiguration {

	public Class<DefaultEndpointHandler> getMasterEndpoint();
	public String getPrefix();
	public String getQueueName();

}
