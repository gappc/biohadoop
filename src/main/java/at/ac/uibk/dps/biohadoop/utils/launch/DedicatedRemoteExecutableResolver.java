package at.ac.uibk.dps.biohadoop.utils.launch;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
				|| dedicatedMasterConfiguration.getAnnotation() == null) {
			return null;
		}

		Class<? extends MasterEndpoint> masterEndpointClass = dedicatedMasterConfiguration
				.getMaster();
		Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass = dedicatedMasterConfiguration
				.getRemoteExecutable();
		Class<? extends Annotation> annotationClass = dedicatedMasterConfiguration
				.getAnnotation();

		Annotation annotation = remoteExecutableClass
				.getAnnotation(annotationClass);
		if (annotation == null) {
			LOG.error("Annotation {} not found on {}",
					annotationClass.getCanonicalName(),
					remoteExecutableClass.getCanonicalName());
			return null;
		}

		Method queueNameMethod = annotationClass.getMethod("queueName",
				new Class[] {});
		if (queueNameMethod == null) {
			return null;
		}
		String queueName = (String) queueNameMethod.invoke(annotation,
				new Object[] {});

		MasterEndpoint masterEndpoint = masterEndpointClass.newInstance();

		return new LaunchInformation(remoteExecutableClass, masterEndpoint,
				queueName);
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
			boolean remoteExecutablesMatch = workerConfiguration
					.getRemoteExecutable() != null
					&& workerConfiguration.getRemoteExecutable().equals(
							launchInformation.getRemoteExecutable());
			if (isLocalWorkerEndpoint && remoteExecutablesMatch) {
				Integer count = workerConfiguration.getCount();
				for (int i = 0; i < count; i++) {
					finalLaunchInformations.add(launchInformation);
				}
			}
		}
		return finalLaunchInformations;
	}

	// private static List<LaunchInformation> handleLocalMasters(
	// CommunicationConfiguration communicationConfiguration,
	// List<LaunchInformation> launchInformations) {
	// List<LaunchInformation> finalLaunchInformations = new
	// ArrayList<LaunchInformation>();
	// for (Iterator<LaunchInformation> iterator = launchInformations
	// .iterator(); iterator.hasNext();) {
	// LaunchInformation launchInformation = iterator.next();
	// boolean isLocalMasterEndpoint = LocalMasterEndpoint.class
	// .equals(launchInformation.getMaster().getClass());
	// boolean hasRemoteExecutable = launchInformation
	// .getRemoteExecutable() != null;
	// if (isLocalMasterEndpoint && hasRemoteExecutable) {
	// boolean isConfigured = false;
	// for (WorkerConfiguration workerConfiguration : communicationConfiguration
	// .getWorkerConfigurations()) {
	// boolean isLocalWorkerEndpoint = DefaultLocalWorker.class
	// .equals(workerConfiguration.getWorker());
	// boolean remoteExecutablesMatch = workerConfiguration
	// .getRemoteExecutable() != null
	// && workerConfiguration.getRemoteExecutable()
	// .equals(launchInformation
	// .getRemoteExecutable());
	// if (isLocalWorkerEndpoint && remoteExecutablesMatch) {
	// isConfigured = true;
	// Integer count = workerConfiguration.getCount();
	// for (int i = 0; i < count - 1; i++) {
	// finalLaunchInformations.add(launchInformation);
	// }
	// }
	// }
	// if (!isConfigured) {
	// iterator.remove();
	// }
	// }
	// }
	// finalLaunchInformations.addAll(launchInformations);
	// return finalLaunchInformations;
	// }
}
