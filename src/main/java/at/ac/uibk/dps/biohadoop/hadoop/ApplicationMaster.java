package at.ac.uibk.dps.biohadoop.hadoop;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.ApplicationLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLauncher;
import at.ac.uibk.dps.biohadoop.torename.ArgumentChecker;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;
import at.ac.uibk.dps.biohadoop.torename.Hostname;

public class ApplicationMaster {

	private static final Logger LOG = LoggerFactory
			.getLogger(ApplicationMaster.class);

	private YarnConfiguration yarnConfiguration = new YarnConfiguration();

	public static void main(String[] args) {
		LOG.info("ApplicationMaster started at " + Hostname.getHostname());
		long start = System.currentTimeMillis();

		ApplicationMaster master = new ApplicationMaster();
		master.run(args);

		long end = System.currentTimeMillis();
		LOG.info("ApplicationMaster stopped, time: {}ms", end - start);
	}

	public void run(String[] args) {
		if (!checkArguments(args)) {
			return;
		}
		try {
			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfiguration
					.getBiohadoopConfiguration(yarnConfiguration, args[0]);

			List<Future<ApplicationId>> applications = ApplicationLauncher
					.launchApplication(biohadoopConfiguration);

			EndpointLauncher.launchMasterEndpoints(biohadoopConfiguration);

			if (System.getProperty("local") == null) {
				WorkerLauncher.launchWorkers(yarnConfiguration,
						biohadoopConfiguration, args[0]);
			}

			for (Future<ApplicationId> application : applications) {
				ApplicationId applicationId = application.get();
				LOG.info("Finished application with id {}", applicationId);
			}
		} catch (Exception e) {
			LOG.error("Error while launching application", e);
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