package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;

public class EndpointLauncher {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointLauncher.class);
	
	public static void launchMasterEndpoints(final BiohadoopConfiguration config) throws Exception {
		for (String endpoint : config.getEndPoints()) {
			LOG.debug("Starting endpoint {}", endpoint);
			Class.forName(endpoint).newInstance();
		}
	}
}
