package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import java.util.Arrays;

import org.apache.hadoop.yarn.conf.YarnConfiguration;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfigurationUtil;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class WorkerParameters {

	private final Class<? extends Worker> workerEnpoint;
	private final String pipelineName;
	private final String host;
	private final int port;

	private WorkerParameters(Class<? extends Worker> workerEnpoint,
			String pipelineName,
			String host, int port) {
		this.workerEnpoint = workerEnpoint;
		this.pipelineName = pipelineName;
		this.host = host;
		this.port = port;
	}

	public Class<? extends Worker> getWorkerEnpoint() {
		return workerEnpoint;
	}

	public String getPipelineName() {
		return pipelineName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@SuppressWarnings("unchecked")
	public static WorkerParameters getParameters(String[] args)
			throws WorkerException {
		if (args == null) {
			throw new WorkerException("Parameters are null");
		}
		try {
			Class<? extends Worker> workerClass = null;
			if (args[0].length() > 0) {
				workerClass = (Class<? extends Worker>) Class
						.forName(args[0]);
			}
			String pipelineName = null;
			if (args[1].length() > 0) {
				pipelineName = args[1];
			}
			String host = args[2];
			int port = Integer.parseInt(args[3]);

			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationUtil
					.read(new YarnConfiguration(),
							args[4]);
			Environment.setBiohadoopConfiguration(biohadoopConfiguration);
			Environment.setBiohadoopConfigurationPath(args[4]);

			return new WorkerParameters(workerClass, pipelineName, host,
					port);
		} catch (Exception e) {
			throw new WorkerException("Could not parse parameters "
					+ Arrays.toString(args), e);
		}
	}
}
