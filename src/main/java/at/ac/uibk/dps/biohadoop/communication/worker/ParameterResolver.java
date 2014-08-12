package at.ac.uibk.dps.biohadoop.communication.worker;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;

public class ParameterResolver {
	
	public static String resolveHttpParameter(
			WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		String hostname = Environment.get(Environment.HTTP_HOST);
		if (hostname == null) {
			throw new WorkerLaunchException(
					"Could not resolve hostname for WorkerConfiguration: "
							+ workerConfiguration
							+ "; maybe master is not configured/started?");
		}
		String port = Environment.get(Environment.HTTP_PORT);
		if (port == null) {
			throw new WorkerLaunchException(
					"Could not resolve port for WorkerConfiguration: "
							+ workerConfiguration
							+ "; maybe master is not configured/started?");
		}
		return workerConfiguration.getWorker().getCanonicalName() + " "
				+ hostname + " " + port;
	}

	public static String resolveParameter(
			WorkerConfiguration workerConfiguration,
			Class<? extends Annotation> annotation, String envHost,
			String envPort) throws WorkerLaunchException {
		String prefix = DefaultTaskClient.QUEUE_NAME;
		Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass = workerConfiguration
				.getRemoteExecutable();
		try {
			if (remoteExecutableClass != null) {
				Annotation workerAnnotation = remoteExecutableClass
						.getAnnotation(annotation);
				Method method = workerAnnotation.getClass().getMethod(
						"queueName", new Class<?>[] {});
				if (method != null) {
					prefix = (String) method.invoke(workerAnnotation);
				}
			}
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new WorkerLaunchException(
					"Could not read annotation of type "
							+ annotation.getCanonicalName()
							+ " with WorkerConfiguration="
							+ workerConfiguration);
		}

		String hostname = Environment.getPrefixed(prefix, envHost);
		if (hostname == null) {
			throw new WorkerLaunchException(
					"Could not resolve hostname for WorkerConfiguration: "
							+ workerConfiguration
							+ "; maybe master is not configured/started?");
		}
		String port = Environment.getPrefixed(prefix, envPort);
		if (port == null) {
			throw new WorkerLaunchException(
					"Could not resolve port for WorkerConfiguration: "
							+ workerConfiguration
							+ "; maybe master is not configured/started?");
		}

		return workerConfiguration.getWorker().getCanonicalName() + " "
				+ hostname + " " + port;
	}
}
