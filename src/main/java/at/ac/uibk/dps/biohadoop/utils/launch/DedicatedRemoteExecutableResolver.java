package at.ac.uibk.dps.biohadoop.utils.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.MasterConfiguration;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.local.DefaultLocalEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.DefaultLocalWorker;

public class DedicatedRemoteExecutableResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(DedicatedRemoteExecutableResolver.class);

	public static List<LaunchInformation> getDedicatedEndpoints(
			CommunicationConfiguration communicationConfiguration)
			throws ResolveDedicatedEndpointException {
		LOG.debug("Resolving dedicated endpoints");
		List<LaunchInformation> finalLaunchInformations = new ArrayList<>();
		for (MasterConfiguration dedicatedMasterConfiguration : communicationConfiguration
				.getDedicatedMasters()) {
			try {
				LaunchInformation launchInformation = getLaunchInformation(dedicatedMasterConfiguration);
				if (launchInformation == null) {
					throw new ResolveDedicatedEndpointException(
							"MasterConfiguration not complete: "
									+ dedicatedMasterConfiguration);
				}
				if (isLocalMaster(launchInformation)) {
					List<LaunchInformation> localLaunchInformations = handleLocalMaster(
							communicationConfiguration, launchInformation);
					finalLaunchInformations.addAll(localLaunchInformations);
				} else {
					finalLaunchInformations.add(launchInformation);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | InstantiationException
					| NoSuchMethodException | SecurityException e) {
				throw new ResolveDedicatedEndpointException(
						"Error while getting LaunchInformation for MasterConfiguration "
								+ dedicatedMasterConfiguration, e);
			}
		}
		return finalLaunchInformations;
	}

	private static LaunchInformation getLaunchInformation(
			MasterConfiguration dedicatedMasterConfiguration)
			throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException {
		if (dedicatedMasterConfiguration.getMaster() == null
				|| dedicatedMasterConfiguration.getRemoteExecutable() == null
				|| dedicatedMasterConfiguration.getSettingName() == null) {
			return null;
		}

		Class<? extends MasterEndpoint> masterEndpointClass = dedicatedMasterConfiguration
				.getMaster();
		Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass = dedicatedMasterConfiguration
				.getRemoteExecutable();
		String settingName = dedicatedMasterConfiguration.getSettingName();

		MasterEndpoint masterEndpoint = masterEndpointClass.newInstance();

		return new LaunchInformation(remoteExecutableClass, masterEndpoint,
				settingName);
	}

	private static boolean isLocalMaster(LaunchInformation launchInformation) {
		return DefaultLocalEndpoint.class.equals(launchInformation.getMaster()
				.getClass());
	}

	private static List<LaunchInformation> handleLocalMaster(
			CommunicationConfiguration communicationConfiguration,
			LaunchInformation launchInformation) {
		List<LaunchInformation> finalLaunchInformations = new ArrayList<>();
		if (launchInformation == null) {
			return finalLaunchInformations;
		}

		for (WorkerConfiguration workerConfiguration : communicationConfiguration
				.getWorkerConfigurations()) {
			boolean isLocalWorkerEndpoint = DefaultLocalWorker.class
					.equals(workerConfiguration.getWorker());
			if (isLocalWorkerEndpoint) {
				Integer count = workerConfiguration.getCount();
				for (int i = 0; i < count; i++) {
					finalLaunchInformations.add(launchInformation);
				}
			}
		}
		return finalLaunchInformations;
	}
}
