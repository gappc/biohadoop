package at.ac.uibk.dps.biohadoop.utils.launch;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;

public class DedicatedRemoteExecutableResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(DedicatedRemoteExecutableResolver.class);

	public static List<LaunchInformation> getDedicatedEndpoints(
			List<Class<? extends RemoteExecutable<?, ?, ?>>> remoteExecutables)
			throws ResolveDedicatedEndpointException {
		List<LaunchInformation> dedicatedEndpoints = new ArrayList<>();
		if (remoteExecutables == null || remoteExecutables.size() == 0) {
			return dedicatedEndpoints;
		}

		for (Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable : remoteExecutables) {
			try {
				List<LaunchInformation> launchInformation = getSuitableClasses(remoteExecutable);
				dedicatedEndpoints.addAll(launchInformation);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | InstantiationException e) {
				throw new ResolveDedicatedEndpointException(
						"Error while resolving annotations for RemoteExecutable "
								+ remoteExecutable, e);
			}
		}
		return dedicatedEndpoints;
	}

	private static List<LaunchInformation> getSuitableClasses(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, InstantiationException {
		List<LaunchInformation> launchInformations = new ArrayList<>();

		Annotation[] annotations = remoteExecutable.getAnnotations();
		for (Annotation annotation : annotations) {
			LOG.debug("Found annotation {} on remote executable {}",
					annotation, remoteExecutable);

			LaunchInformation launchInformation = getLaunchInforamtion(
					remoteExecutable, annotation);
			if (launchInformation != null) {
				launchInformations.add(launchInformation);
			}
		}

		return launchInformations;
	}

	@SuppressWarnings("unchecked")
	private static LaunchInformation getLaunchInforamtion(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			Annotation annotation) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			InstantiationException {
		Class<? extends MasterLifecycle> masterClass = null;
		String queueName = null;

		Method[] methods = annotation.annotationType().getMethods();
		for (Method method : methods) {
			LOG.debug("Found method {} for annotation {}", method, annotation);
			if ("master".equals(method.getName())) {
				masterClass = (Class<? extends MasterLifecycle>) method
						.invoke(annotation);
			}
			if ("queueName".equals(method.getName())) {
				queueName = (String) method.invoke(annotation);

				// Dedicated endpoints can not work with default queue, hence
				// set queueName to null - this way the entry is not recognized
				// as a result
				if (DefaultTaskClient.QUEUE_NAME.equals(queueName)) {
					queueName = null;
				}
			}
		}

		if (masterClass != null && queueName != null) {
			MasterLifecycle master = masterClass.newInstance();
			return new LaunchInformation(remoteExecutable, master, queueName);
		}

		return null;
	}
}
