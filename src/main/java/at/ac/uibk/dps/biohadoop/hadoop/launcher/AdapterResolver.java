package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterConfiguration;

public class AdapterResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(AdapterResolver.class);

	public static List<Adapter> getAdapters(
			CommunicationConfiguration communicationConfiguration)
			throws AdapterLaunchException {

		if (communicationConfiguration.getAdapters() == null) {
			throw new AdapterLaunchException(
					"CommunicationConfiguration is incomplete, no adapters defined");
		}

		LOG.debug("Resolving adapters");
		List<Adapter> adapters = new ArrayList<>();
		for (AdapterConfiguration adapterConfiguration : communicationConfiguration
				.getAdapters()) {

			if (adapterConfiguration.getAdapter() == null) {
				throw new AdapterLaunchException(
						"AdapterConfiguration is null");
			}

			try {
				Class<? extends Adapter> adapterClass = adapterConfiguration
						.getAdapter();
				Adapter adapter = adapterClass.newInstance();
				adapters.add(adapter);
			} catch (IllegalAccessException | IllegalArgumentException
					| InstantiationException | SecurityException e) {
				throw new AdapterLaunchException(
						"Error while getting Adapter for AdapterConfiguration "
								+ adapterConfiguration, e);
			}
		}
		return adapters;
	}
}
