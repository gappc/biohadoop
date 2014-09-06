package at.ac.uibk.dps.biohadoop.communication.worker;

import java.util.Arrays;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfigurationUtil;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class WorkerParameters {

	private final Class<? extends WorkerEndpoint> workerEnpoint;
	private final String settingName;
	private final String host;
	private final int port;

	private WorkerParameters(Class<? extends WorkerEndpoint> workerEnpoint,
			String settingName,
			String host, int port) {
		this.workerEnpoint = workerEnpoint;
		this.settingName = settingName;
		this.host = host;
		this.port = port;
	}

	public Class<? extends WorkerEndpoint> getWorkerEnpoint() {
		return workerEnpoint;
	}

	public String getSettingName() {
		return settingName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends RemoteExecutable<?, ?, ?>> getLocalParameters(
			String[] args) throws WorkerException {
		if (args == null || args.length == 0) {
			throw new WorkerException("Parameters are null");
		}
		if (args[0] == null || "".equals(args[0])) {
			return null;
		}
		try {
			return (Class<? extends RemoteExecutable<?, ?, ?>>) Class
					.forName(args[0]);
		} catch (ClassNotFoundException e) {
			throw new WorkerException("Could not parse parameters "
					+ Arrays.toString(args), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static WorkerParameters getParameters(String[] args)
			throws WorkerException {
		if (args == null) {
			throw new WorkerException("Parameters are null");
		}
		try {
			Class<? extends WorkerEndpoint> workerEndpoint = null;
			if (args[0].length() > 0) {
				workerEndpoint = (Class<? extends WorkerEndpoint>) Class
						.forName(args[0]);
			}
			String settingName = null;
			if (args[1].length() > 0) {
				settingName = args[1];
			}
			String host = args[2];
			int port = Integer.parseInt(args[3]);

			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationUtil
					.read(new YarnConfiguration(),
							args[4]);
			Environment.setBiohadoopConfiguration(biohadoopConfiguration);
			Environment.setBiohadoopConfigurationPath(args[4]);

			return new WorkerParameters(workerEndpoint, settingName, host,
					port);
		} catch (Exception e) {
			throw new WorkerException("Could not parse parameters "
					+ Arrays.toString(args), e);
		}
	}
}
