package at.ac.uibk.dps.biohadoop.ga;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.ApplicationLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.torename.BiohadoopConfigurationReader;

public class GaMain {

	private static final Logger LOG = LoggerFactory.getLogger(GaMain.class);

	public static void main(String[] args) {
		try {
			YarnConfiguration yarnConfiguration = new YarnConfiguration();
			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationReader
					.readBiohadoopConfiguration(yarnConfiguration, args[0]);
			
			List<Future<ApplicationId>> algorithms = ApplicationLauncher
					.launchApplication(biohadoopConfiguration);
			
			EndpointLauncher.launchMasterEndpoints(biohadoopConfiguration);
			
			for (Future<ApplicationId> algorithm : algorithms) {
				ApplicationId applicationId = algorithm.get();
				LOG.info("{} finished", applicationId);
			}
		} catch (Exception e) {
			LOG.error("Exception while running GaMain", e);
		}
	}
}
