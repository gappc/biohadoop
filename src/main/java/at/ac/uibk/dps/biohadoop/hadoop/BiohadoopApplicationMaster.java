package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.AdapterLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.SolverLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLauncher;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.HdfsUtil;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;

public class BiohadoopApplicationMaster {

	private static final Logger LOG = LoggerFactory
			.getLogger(BiohadoopApplicationMaster.class);

	private static final String CLASSNAME = ClassnameProvider
			.getClassname(BiohadoopApplicationMaster.class);

	private YarnConfiguration yarnConfiguration = new YarnConfiguration();

	public static void main(String[] args) {
		try {
			LOG.info("{} started at {}", CLASSNAME, HostInfo.getHostname());
			long start = System.currentTimeMillis();

			BiohadoopApplicationMaster master = new BiohadoopApplicationMaster();
			master.checkArguments(args);
			master.run(args);

			long end = System.currentTimeMillis();
			LOG.info("{} stopped, time: {}ms", CLASSNAME, end - start);
		} catch (Exception e) {
			LOG.error("Error while running {}", CLASSNAME, e);
			System.exit(1);
		}
	}

	public void run(String[] args) throws Exception {
		BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationUtil
				.read(yarnConfiguration, args[0]);
		Environment.setBiohadoopConfiguration(biohadoopConfiguration);
		Environment.setBiohadoopConfigurationPath(args[0]);

		AdapterLauncher adapterLauncher = new AdapterLauncher(
				biohadoopConfiguration);
		adapterLauncher.startAdapters();

		if (System.getProperty("local") == null) {
			WorkerLauncher.launchWorkers(yarnConfiguration,
					biohadoopConfiguration, args[0]);
		} else {
			WorkerLauncher.pretendToLaunchWorkers(biohadoopConfiguration);
		}

		List<Future<SolverId>> solvers = SolverLauncher
				.launchSolver(biohadoopConfiguration);
		
		for (Future<SolverId> solver : solvers) {
			try {
				SolverId solverId = solver.get();
				LOG.info("Finished solver with id {}", solverId);
			} catch (ExecutionException e) {
				// TODO add counter to detect errors and set Biohadoops result
				// accordingly?
				LOG.error("Error while running Algorithm", e.getCause());
			}
		}
		LOG.info("All solvers finished");

		LOG.info("Stopping all communication");
		adapterLauncher.stopAdapters();
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