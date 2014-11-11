package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.EndpointException;

public class EndpointLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	
	private List<Endpoint> endpoints = new ArrayList<>();

	public EndpointLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	public void startEndpoints() throws EndpointException {
		try {
			LOG.info("Adding endpoints");
			endpoints = EndpointResolver.getEndpoints(communicationConfiguration);

			if (endpoints.size() == 0) {
				throw new EndpointException("No usable endpoints found");
			}

			LOG.info("Starting endpoints");
			for (Endpoint endpoint : endpoints) {
				LOG.debug("Starting endpoint {}", endpoint);
				endpoint.start();
			}
		} catch (EndpointLaunchException e) {
			throw new EndpointException(e);
		}
	}

	public void stopEndpoints() throws EndpointException {
		LOG.info("Stopping endpoint");
		for (Endpoint endpoint : endpoints) {
			LOG.debug("Stopping endpoint {}", endpoint);
			endpoint.stop();
		}
	}
}
