package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.AlgorithmLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLauncher;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;

public class BiohadoopApplicationMaster {

	private static final Logger LOG = LoggerFactory
			.getLogger(BiohadoopApplicationMaster.class);

	private YarnConfiguration yarnConfiguration = new YarnConfiguration();

	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			LOG.info("Started Biohadoop at {}, System.nanoTime()={}",
					HostInfo.getHostname(), start);
			Environment.set(Environment.BIOHADOOP_START_AT_NS, Long.toString(start));

			BiohadoopApplicationMaster master = new BiohadoopApplicationMaster();
			master.checkArguments(args);
			master.run(args);

			long end = System.currentTimeMillis();
			LOG.info("Biohadoop stopped, time: {}ms", end - start);
		} catch (Exception e) {
			LOG.error("Error while running Biohadoop", e);
			System.exit(1);
		}
	}

	public void run(String[] args) throws Exception {
		BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationUtil
				.read(yarnConfiguration, args[0]);
		Environment.setBiohadoopConfiguration(biohadoopConfiguration);
		Environment.setBiohadoopConfigurationPath(args[0]);

		EndpointLauncher endpointLauncher = new EndpointLauncher(
				biohadoopConfiguration);
		endpointLauncher.startEndpoints();

		if (System.getProperty("local") == null) {
			WorkerLauncher.launchWorkers(yarnConfiguration,
					biohadoopConfiguration, args[0]);
		} else {
			WorkerLauncher.pretendToLaunchWorkers(biohadoopConfiguration);
		}

		List<Future<AlgorithmId>> algorithmFutures = AlgorithmLauncher
				.launchAlgorithm(biohadoopConfiguration);

		for (Future<AlgorithmId> algorithmFuture : algorithmFutures) {
			try {
				AlgorithmId algorithmId = algorithmFuture.get();
				LOG.info("Finished algorithm with id {}", algorithmId);
			} catch (ExecutionException e) {
				// TODO add counter to detect errors and set Biohadoops result
				// accordingly?
				LOG.error("Error while running Algorithm", e.getCause());
			}
		}
		LOG.info("All algorithms finished");

		LOG.info("Stopping all communication");
		endpointLauncher.stopEndpoints();
	}

	private void checkArguments(String[] args) {
		LOG.info("Checking arguments");

		if (args.length != 1) {
			LOG.error("Wrong number of arguments, got {}, expected {}",
					args.length, 1);
			throw new IllegalArgumentException();
		}
		if (!HdfsUtil.exists(yarnConfiguration, args[0])) {
			throw new IllegalArgumentException("Configuration file " + args[0]
					+ " could not be found");
		}
	}

}