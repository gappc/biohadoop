package at.ac.uibk.dps.biohadoop.utils.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.AdapterConfiguration;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.local.LocalAdapter;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;

public class DedicatedRemoteExecutableResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(DedicatedRemoteExecutableResolver.class);

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
				if (isLocalAdapter(launchInformation)) {
					List<LaunchInformation> localLaunchInformations = handleLocalAdapter(
							communicationConfiguration, launchInformation);
					finalLaunchInformations.addAll(localLaunchInformations);
				} else {
					finalLaunchInformations.add(launchInformation);
				}
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
				|| dedicatedAdapterConfiguration.getSettingName() == null) {
			return null;
		}

		Class<? extends Adapter> adapterClass = dedicatedAdapterConfiguration
				.getAdapter();
		String settingName = dedicatedAdapterConfiguration.getSettingName();

		Adapter adapter = adapterClass.newInstance();

		return new LaunchInformation(adapter, settingName);
	}

	private static boolean isLocalAdapter(LaunchInformation launchInformation) {
		return LocalAdapter.class.equals(launchInformation.getAdapter()
				.getClass());
	}

	private static List<LaunchInformation> handleLocalAdapter(
			CommunicationConfiguration communicationConfiguration,
			LaunchInformation launchInformation) {
		List<LaunchInformation> finalLaunchInformations = new ArrayList<>();
		if (launchInformation == null) {
			return finalLaunchInformations;
		}

		for (WorkerConfiguration workerConfiguration : communicationConfiguration
				.getWorkerConfigurations()) {
			boolean isLocalWorker = LocalWorker.class
					.equals(workerConfiguration.getWorker());
			if (isLocalWorker) {
				Integer count = workerConfiguration.getCount();
				for (int i = 0; i < count; i++) {
					finalLaunchInformations.add(launchInformation);
				}
			}
		}
		return finalLaunchInformations;
	}
}
