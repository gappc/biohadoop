package at.ac.uibk.dps.biohadoop.moead;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.Application;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Moead;
import at.ac.uibk.dps.biohadoop.moead.algorithm.Printer;

public class MoeadMain {

	private static final Logger LOG = LoggerFactory
			.getLogger(MoeadMain.class);
	
	private static final int ITERATIONS = 10000;
	private static final int POPULATION_SIZE = 300;
	private static final int NEIGHBOR_SIZE = 290;
	private static final int GENOME_SIZE = 100;

	public static void main(String[] args) {
		Application application = new Application("MOEAD-" + Thread.currentThread().getName());
		ApplicationManager applicationManager = ApplicationManager.getInstance();
		final ApplicationId applicationId = applicationManager.addApplication(application);

		try {
			Moead moead = new Moead(applicationId);
			List<List<Double>> solution = moead.moead(ITERATIONS, POPULATION_SIZE, NEIGHBOR_SIZE, GENOME_SIZE);
			Printer.printSolution(solution);
			saveToFile("/tmp/moead-sol.txt", solution);
		} catch (InterruptedException e) {
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
			e.printStackTrace();
		}
	}
}
