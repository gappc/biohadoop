package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.SolverLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLauncher;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.torename.ArgumentChecker;
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
		LOG.info("{} started at {}", className, HostInfo.getHostname());
		long start = System.currentTimeMillis();

		BiohadoopApplicationMaster master = new BiohadoopApplicationMaster();
		master.run(args);

		long end = System.currentTimeMillis();
		LOG.info("{} stopped, time: {}ms", className, end - start);
	}

	public void run(String[] args) {
		if (!checkArguments(args)) {
			return;
		}
		try {
			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationReader
					.readBiohadoopConfiguration(yarnConfiguration, args[0]);
			
			Environment.setBiohadoopConfiguration(biohadoopConfiguration);

			EndpointLauncher.launchMasterEndpoints(biohadoopConfiguration);

			List<Future<SolverId>> solvers = SolverLauncher
					.launchSolver(biohadoopConfiguration);

			if (System.getProperty("local") == null) {
				WorkerLauncher.launchWorkers(yarnConfiguration,
						biohadoopConfiguration, args[0]);
			}
			else {
				WorkerLauncher.pretendToLaunchWorkers(biohadoopConfiguration);
			}

			for (Future<SolverId> solver : solvers) {
				SolverId solverId = solver.get();
				LOG.debug("Finished solver with id {}", solverId);
			}
		} catch (Exception e) {
			LOG.error("Error while launching solver", e);
		}
	}

	private boolean checkArguments(String[] args) {
		LOG.info("Checking arguments");

		if (!ArgumentChecker.isArgumentCountValid(args, 1)) {
			return false;
		}
		if (!HdfsUtil.exists(yarnConfiguration, args[0])) {
			return false;
		}
		return true;
	}

}