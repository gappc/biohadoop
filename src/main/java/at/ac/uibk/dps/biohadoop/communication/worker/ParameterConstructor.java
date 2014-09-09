package at.ac.uibk.dps.biohadoop.communication.worker;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;

public class ParameterConstructor {

	public static String resolveHttpParameter(
			WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		String settingName = workerConfiguration.getSettingName();
		String hostname = getEnvironmentData(workerConfiguration,
				Environment.DEFAULT_PREFIX, Environment.HTTP_HOST);
		String port = getEnvironmentData(workerConfiguration,
				Environment.DEFAULT_PREFIX, Environment.HTTP_PORT);

		return workerConfiguration.getWorker().getCanonicalName() + " "
				+ settingName + " " + hostname + " " + port + " "
				+ Environment.getBiohadoopConfigurationPath();
	}

	public static String resolveParameter(
			WorkerConfiguration workerConfiguration, String envHost,
			String envPort) throws WorkerLaunchException {
		String settingName = workerConfiguration.getSettingName();
		String hostname = getEnvironmentData(workerConfiguration, settingName,
				envHost);
		String port = getEnvironmentData(workerConfiguration, settingName,
				envPort);

		return workerConfiguration.getWorker().getCanonicalName() + " "
				+ settingName + " " + hostname + " " + port + " "
				+ Environment.getBiohadoopConfigurationPath();
	}

	private static String getEnvironmentData(
			WorkerConfiguration workerConfiguration, String prefix, String key)
			throws WorkerLaunchException {
		String value = null;
		int count = 0;
		// Need to do some polling, as requested values may be empty, e.g. when
		// socket adapter hasn't started -> host/port binding takes some time
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
								+ "retries, maybe adapter is not configured/started? WorkerConfiguration: "
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

}
