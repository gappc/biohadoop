package at.ac.uibk.dps.biohadoop.ga.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.ga.worker.LocalGaWorker;
import at.ac.uibk.dps.biohadoop.job.JobManager;
import at.ac.uibk.dps.biohadoop.job.Task;
import at.ac.uibk.dps.biohadoop.queue.Monitor;

public class Ga {

	public static final String GA_WORK_QUEUE = "GA_WORK_QUEUE";
	public static final String GA_RESULT_STORE = "GA_RESULT_STORE";

	private static final Logger LOGGER = LoggerFactory.getLogger(Ga.class);
	
	private Random rand = new Random();
	private JobManager jobManager = JobManager.getInstance();
	private Monitor monitor = jobManager.getResultStoreMonitor(GA_RESULT_STORE);

	public int[] ga(Tsp tsp, int genomeSize, int maxIterations)
			throws InterruptedException {
		new Thread(new LocalGaWorker()).start();

		int citySize = tsp.getCities().length;

		boolean end = false;
		int counter = 0;

		// Init: Generate random population
		int[][] population = initPopulation(genomeSize, citySize);

		long start = System.currentTimeMillis();

		while (!end) {
			// recombination
			int[][] offsprings = new int[genomeSize][citySize];
			for (int i = 0; i < genomeSize; i++) {
				int indexParent1 = rand.nextInt(genomeSize);
				int indexParent2 = rand.nextInt(genomeSize);

				offsprings[i] = recombination(population[indexParent1],
						population[indexParent2]);
			}

			// mutation
			int[][] mutated = new int[genomeSize][citySize];
			for (int i = 0; i < genomeSize; i++) {
				mutated[i] = mutation(offsprings[i]);
			}

			// evaluation
			double[] values = new double[genomeSize * 2];
			for (int i = 0; i < genomeSize; i++) {
				GaTask task = new GaTask(i, population[i]);
				jobManager.scheduleTask(GA_WORK_QUEUE, task);
			}
			for (int i = 0; i < genomeSize; i++) {
				GaTask task = new GaTask(i + genomeSize, population[i]);
				jobManager.scheduleTask(GA_WORK_QUEUE, task);
			}

			synchronized (monitor) {
				if (!monitor.isWasSignalled()) {
					monitor.wait();
				}
				monitor.setWasSignalled(false);
			}

			Task[] results = jobManager.readResult(GA_RESULT_STORE);
			for (int i = 0; i < genomeSize; i++) {
				values[i] = (double) ((GaResult) results[i]).getResult();
			}
			for (int i = 0; i < genomeSize; i++) {
				values[i + genomeSize] = (double) ((GaResult) results[i
						+ genomeSize]).getResult();
			}

			// selection
			for (int i = 0; i < genomeSize * 2; i++) {
				for (int j = i + 1; j < genomeSize * 2; j++) {
					if (values[j] < values[i]) {
						double tmp = values[j];
						values[j] = values[i];
						values[i] = tmp;

						int[] tmpGenome;
						if (i < genomeSize && j < genomeSize) {
							// i and j refer to population
							tmpGenome = population[i];
							population[i] = population[j];
							population[j] = tmpGenome;
						} else if (i >= genomeSize && j < genomeSize) {
							// i refer to mutated, j refer to population
							tmpGenome = mutated[i - genomeSize];
							mutated[i - genomeSize] = population[j];
							population[j] = tmpGenome;
						} else if (i < genomeSize && j >= genomeSize) {
							// i refer to population, j refer to mutated
							tmpGenome = population[i];
							population[i] = mutated[j - genomeSize];
							mutated[j - genomeSize] = tmpGenome;
						} else {
							// i and j refer to mutated
							tmpGenome = mutated[i - genomeSize];
							mutated[i - genomeSize] = mutated[j - genomeSize];
							mutated[j - genomeSize] = tmpGenome;
						}
					}
				}
			}
			counter++;
			if (counter == maxIterations) {
				end = true;
			}

			if (counter % 1e3 == 0 || counter < 10) {
				LOGGER.info("counter: " + counter + " | took "
						+ (System.currentTimeMillis() - start) + "ms");
				start = System.currentTimeMillis();
				printGenome(tsp.getDistances(), population[0], citySize);
			}
		}
		
		jobManager.stopAllWorkers();

		return population[0];
	}

	private int[][] initPopulation(int genomeSize, int citieSize) {
		int[][] population = new int[genomeSize][citieSize];

		for (int i = 0; i < genomeSize; i++) {
			List<Integer> singlePopulation = new ArrayList<Integer>();
			for (int j = 0; j < citieSize; j++) {
				singlePopulation.add(j);
			}
			Collections.shuffle(singlePopulation);

			for (int j = 0; j < citieSize; j++) {
				population[i][j] = singlePopulation.get(j);
			}
		}
		return population;
	}

	// based on Partially - Mapped Crossover (PMX) and
	// http://www.ceng.metu.edu.tr/~ucoluk/research/publications/tspnew.pdf
	private int[] recombination(int[] ds, int[] ds2) {
		int size = ds.length;

		int[] result = new int[size];
		for (int i = 0; i < size; i++) {
			result[i] = ds[i];
		}

		for (int i = 0; i < size / 2; i++) {
			int swapPos = findElementPos(result, ds2[i]);
			int tmp = result[i];
			result[i] = result[swapPos];
			result[swapPos] = tmp;
		}
		return result;
	}

	private int findElementPos(int[] list, int element) {
		for (int i = 0; i < list.length; i++) {
			if (list[i] == element) {
				return i;
			}
		}
		return -1;
	}

	private int[] mutation(int[] ds) {
		int pos1 = rand.nextInt(ds.length);
		int pos2 = rand.nextInt(ds.length);

		int tmp = ds[pos2];
		ds[pos2] = ds[pos1];
		ds[pos1] = tmp;

		return ds;
	}

	private void printGenome(double[][] distances, int[] solution, int citySize) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < citySize; i++) {
			sb.append(solution[i] + " | ");
		}
		LOGGER.info("fitness: " + GaFitness.computeFitness(distances, solution) + " | "
				+ sb.toString());
	}

}
