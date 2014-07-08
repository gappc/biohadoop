package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.SolverLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WeldLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLauncher;
import at.ac.uibk.dps.biohadoop.queue.TaskQueueService;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.torename.BiohadoopConfigurationReader;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;
import at.ac.uibk.dps.biohadoop.torename.Helper;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

public class BiohadoopApplicationMaster {

	private static final Logger LOG = LoggerFactory
			.getLogger(BiohadoopApplicationMaster.class);

	private final static String className = Helper
			.getClassname(BiohadoopApplicationMaster.class);

	private YarnConfiguration yarnConfiguration = new YarnConfiguration();

	public static void main(String[] args) {
		try {
			LOG.info("{} started at {}", className, HostInfo.getHostname());
			long start = System.currentTimeMillis();

			BiohadoopApplicationMaster master = new BiohadoopApplicationMaster();
			master.checkArguments(args);
			master.run(args);

			long end = System.currentTimeMillis();
			LOG.info("{} stopped, time: {}ms", className, end - start);
		} catch (Exception e) {
			LOG.error("Error while running {}", className, e);
			System.exit(1);
		}
	}

	public void run(String[] args) throws Exception {
		BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationReader
				.readBiohadoopConfiguration(yarnConfiguration, args[0]);
		Environment.setBiohadoopConfiguration(biohadoopConfiguration);

		WeldLauncher.startWeld();
		
		EndpointLauncher endpointLauncher = new EndpointLauncher(
				biohadoopConfiguration);
		endpointLauncher.startMasterEndpoints();

		List<Future<SolverId>> solvers = SolverLauncher
				.launchSolver(biohadoopConfiguration);

		if (System.getProperty("local") == null) {
			WorkerLauncher.launchWorkers(yarnConfiguration,
					biohadoopConfiguration, args[0]);
		} else {
			WorkerLauncher.pretendToLaunchWorkers(biohadoopConfiguration);
		}
		
		for (Future<SolverId> solver : solvers) {
			SolverId solverId = solver.get();
			LOG.info("Finished solver with id {}", solverId);
		}

		TaskQueueService.getInstance().stopAllTaskQueues();
		endpointLauncher.stopMasterEndpoints();
		
		WeldLauncher.stopWeld();
	}

	private void checkArguments(String[] args) {
		LOG.info("Checking arguments");

		if (args.length != 1) {
			LOG.error("Wrong number of arguments, got {}, expected {}",
					args.length, 1);
			throw new IllegalArgumentException();
		}
		if (!HdfsUtil.exists(yarnConfiguration, args[0])) {
			throw new IllegalArgumentException();
		}
	}

}