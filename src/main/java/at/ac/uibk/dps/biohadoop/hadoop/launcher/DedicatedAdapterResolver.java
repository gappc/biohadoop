package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterConfiguration;

public class DedicatedAdapterResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(DedicatedAdapterResolver.class);

	public static List<LaunchInformation> getDedicatedAdapters(
			CommunicationConfiguration communicationConfiguration)
			throws ResolveDedicatedAdapterException {
		LOG.debug("Resolving dedicated adapters");
		List<LaunchInformation> finalLaunchInformations = new ArrayList<>();
		for (AdapterConfiguration dedicatedAdapterConfiguration : communicationConfiguration
				.getDedicatedAdapters()) {
			try {
				LaunchInformation launchInformation = getLaunchInformation(dedicatedAdapterConfiguration);
				if (launchInformation == null) {
					throw new ResolveDedicatedAdapterException(
							"AdapterConfiguration not complete: "
									+ dedicatedAdapterConfiguration);
				}
				finalLaunchInformations.add(launchInformation);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | InstantiationException
					| NoSuchMethodException | SecurityException e) {
				throw new ResolveDedicatedAdapterException(
						"Error while getting LaunchInformation for AdapterConfiguration "
								+ dedicatedAdapterConfiguration, e);
			}
		}
		return finalLaunchInformations;
	}

	private static LaunchInformation getLaunchInformation(
			AdapterConfiguration dedicatedAdapterConfiguration)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException {
		if (dedicatedAdapterConfiguration.getAdapter() == null
				|| dedicatedAdapterConfiguration.getPipelineName() == null) {
			return null;
		}

		Class<? extends Adapter> adapterClass = dedicatedAdapterConfiguration
				.getAdapter();
		Adapter adapter = adapterClass.newInstance();
		String pipelineName = dedicatedAdapterConfiguration.getPipelineName();

		return new LaunchInformation(adapter, pipelineName);
	}
}
