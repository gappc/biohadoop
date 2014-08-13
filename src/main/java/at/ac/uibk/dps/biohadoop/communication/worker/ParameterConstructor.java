package at.ac.uibk.dps.biohadoop.communication.worker;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;

public class ParameterConstructor {

	public static String resolveHttpParameter(
			WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		String hostname = getEnvironmentData(workerConfiguration,
				Environment.DEFAULT_PREFIX, Environment.HTTP_HOST);
		String port = getEnvironmentData(workerConfiguration,
				Environment.DEFAULT_PREFIX, Environment.HTTP_PORT);
		String remoteExecutableClassName = getRemoteExecutableClassName(workerConfiguration);

		return workerConfiguration.getWorker().getCanonicalName() + " "
				+ remoteExecutableClassName + " " + hostname + " " + port;
	}

	public static String resolveParameter(
			WorkerConfiguration workerConfiguration,
			Class<? extends Annotation> annotation, String envHost,
			String envPort) throws WorkerLaunchException {
		String prefix = getPrefix(workerConfiguration, annotation);
		String hostname = getEnvironmentData(workerConfiguration, prefix,
				envHost);
		String port = getEnvironmentData(workerConfiguration, prefix, envPort);
		String remoteExecutableClassName = getRemoteExecutableClassName(workerConfiguration);

		return workerConfiguration.getWorker().getCanonicalName() + " "
				+ remoteExecutableClassName + " " + hostname + " " + port;
	}

	private static String getEnvironmentData(
			WorkerConfiguration workerConfiguration, String prefix, String key)
			throws WorkerLaunchException {
		String value = null;
		int count = 0;
		// Need to do some polling, as requested values may be empty, e.g. when
		// socket master hasn't started -> host/port binding takes some time
		while ((value = Environment.getPrefixed(prefix, key)) == null) {
			count++;
			if (count == 10) {
				throw new WorkerLaunchException(
						"Could not resolve "
								+ prefix
								+ "_"
								+ key
								+ "="
								+ value
								+ " after "
								+ count
								+ "retries, maybe master is not configured/started? WorkerConfiguration: "
								+ workerConfiguration);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new WorkerLaunchException("Error while waiting for "
						+ prefix + "_" + key + "=" + value, e);
			}
		}
		return value;
	}

	private static String getRemoteExecutableClassName(
			WorkerConfiguration workerConfiguration) {
		return workerConfiguration.getRemoteExecutable() == null ? "''"
				: workerConfiguration.getRemoteExecutable().getCanonicalName();
	}

	private static String getPrefix(WorkerConfiguration workerConfiguration,
			Class<? extends Annotation> annotation)
			throws WorkerLaunchException {
		String prefix = DefaultTaskClient.QUEUE_NAME;
		Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass = workerConfiguration
				.getRemoteExecutable();
		try {
			if (remoteExecutableClass != null) {
				Annotation workerAnnotation = remoteExecutableClass
						.getAnnotation(annotation);
				if (workerAnnotation != null) {
					Method method = workerAnnotation.getClass().getMethod(
							"queueName", new Class<?>[] {});
					if (method != null) {
						prefix = (String) method.invoke(workerAnnotation);
					}
				} else {

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
		return prefix;
	}

}
