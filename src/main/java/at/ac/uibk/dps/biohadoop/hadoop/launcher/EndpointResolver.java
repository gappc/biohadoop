package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.Endpoint;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.EndpointConfiguration;

public class EndpointResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointResolver.class);

	public static List<Endpoint> getEndpoints(
			CommunicationConfiguration communicationConfiguration)
			throws EndpointLaunchException {

		if (communicationConfiguration.getEndpoints() == null) {
			throw new EndpointLaunchException(
					"CommunicationConfiguration is incomplete, no endpoints defined");
		}

		LOG.debug("Resolving endpoints");
		List<Endpoint> endpoints = new ArrayList<>();
		for (EndpointConfiguration endpointConfiguration : communicationConfiguration
				.getEndpoints()) {

			if (endpointConfiguration.getEndpoint() == null) {
				throw new EndpointLaunchException(
						"EndpointConfiguration is null");
			}

			try {
				Class<? extends Endpoint> endpointClass = endpointConfiguration
						.getEndpoint();
				Endpoint endpoint = endpointClass.newInstance();
				endpoints.add(endpoint);
			} catch (IllegalAccessException | IllegalArgumentException
					| InstantiationException | SecurityException e) {
				throw new EndpointLaunchException(
						"Error while getting endpoint for EndpointConfiguration "
								+ endpointConfiguration, e);
			}
		}
		return endpoints;
	}
}
