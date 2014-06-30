package at.ac.uibk.dps.biohadoop.solver.moead;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.SolverLauncher;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.EndpointLauncher;
import at.ac.uibk.dps.biohadoop.service.solver.SolverConfiguration;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;
import at.ac.uibk.dps.biohadoop.solver.moead.config.MoeadAlgorithmConfig;
import at.ac.uibk.dps.biohadoop.torename.BiohadoopConfigurationReader;

public class MoeadMain {

	private static final Logger LOG = LoggerFactory.getLogger(MoeadMain.class);

	public static void main(String[] args) {
		try {
			YarnConfiguration yarnConfiguration = new YarnConfiguration();
			BiohadoopConfiguration biohadoopConfiguration = BiohadoopConfigurationReader
					.readBiohadoopConfiguration(yarnConfiguration, args[0]);

			List<Future<SolverId>> algorithms = SolverLauncher
					.launchSolver(biohadoopConfiguration);

			EndpointLauncher endpointLauncher = new EndpointLauncher(
					biohadoopConfiguration);
			endpointLauncher.startMasterEndpoints();

			SolverService solverService = SolverService
					.getInstance();
			for (Future<SolverId> algorithm : algorithms) {
				SolverId solverId = algorithm.get();
				LOG.info("{} finished", solverId);
				
				@SuppressWarnings("unchecked")
				List<List<Double>> solution = (List<List<Double>>) solverService
						.getSolverData(solverId);
				SolverConfiguration solverConfiguration = solverService
						.getSolverConfiguration(solverId);
				String outputFilename = ((MoeadAlgorithmConfig) solverConfiguration
						.getAlgorithmConfiguration()).getOutputFile();
				saveToFile(outputFilename, solution);
			}
		} catch (Exception e) {
			LOG.error("Exception while running MoeadMain", e);
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
			LOG.error("Exception while saving Moead data to file {}", filename, e);
		}
	}
}
