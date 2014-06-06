package at.ac.uibk.dps.biohadoop.nsgaii;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationConfiguration;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.ApplicationLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.nsgaii.config.NsgaIIAlgorithmConfig;

public class NsgaIIMain {

	private static final Logger LOG = LoggerFactory.getLogger(NsgaIIMain.class);

	public static void main(String[] args) {
		try {
			YarnConfiguration yarnConfiguration = new YarnConfiguration();
			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfiguration
					.getBiohadoopConfiguration(yarnConfiguration, args[0]);

			List<Future<ApplicationId>> algorithms = ApplicationLauncher
					.launchApplication(biohadoopConfiguration);

			EndpointLauncher.launchMasterEndpoints(biohadoopConfiguration);

			ApplicationManager applicationManager = ApplicationManager
					.getInstance();
			for (Future<ApplicationId> algorithm : algorithms) {
				ApplicationId applicationId = algorithm.get();
				LOG.info("{} finished", applicationId);
				
				@SuppressWarnings("unchecked")
				List<List<Double>> solution = (List<List<Double>>) applicationManager
						.getApplicationData(applicationId);
				ApplicationConfiguration applicationConfiguration = applicationManager
						.getApplicationConfiguration(applicationId);
				String outputFilename = ((NsgaIIAlgorithmConfig) applicationConfiguration
						.getAlgorithmConfiguration()).getOutputFile();
				saveToFile(outputFilename, solution);
			}
		} catch (Exception e) {
			LOG.error("Exception while running NsgaIIMain", e);
		}
	}

	private static void saveToFile(String filename, List<List<Double>> solution) {
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter(filename));
			for (List<Double> l : solution) {
				br.write(l.get(0) + " " + l.get(1) + "\n");
			}
			br.flush();
			br.close();
		} catch (IOException e) {
			LOG.error("Exception while saving NsgaII data to file {}", filename, e);
		}
	}
}
