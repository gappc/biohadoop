package at.ac.uibk.dps.biohadoop.torename;

import at.ac.uibk.dps.biohadoop.endpoint.MasterEndpoint;

public interface MasterConfiguration {

	public Class<? extends MasterEndpoint> getMasterEndpoint();
	public String getPrefix();
	public String getQueueName();

}
