package at.ac.uibk.dps.biohadoop.solver.ga.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.config.AlgorithmException;
import at.ac.uibk.dps.biohadoop.queue.TaskClient;
import at.ac.uibk.dps.biohadoop.queue.TaskClientImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskFuture;
import at.ac.uibk.dps.biohadoop.service.solver.SolverData;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.service.solver.SolverService;
import at.ac.uibk.dps.biohadoop.service.solver.SolverState;
import at.ac.uibk.dps.biohadoop.solver.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.solver.ga.config.GaParameter;

public class Ga implements Algorithm<int[], GaParameter> {

	private static final Logger LOG = LoggerFactory.getLogger(Ga.class);
	public static final String GA_QUEUE = "GA_QUEUE";

	private final Random rand = new Random();
	private final int logSteps = 1000;

	@Override
	public int[] compute(SolverId solverId, GaParameter parameter)
			throws AlgorithmException {
		SolverService solverService = SolverService.getInstance();
		solverService.setSolverState(solverId, SolverState.RUNNING);

		TaskClient<int[], Double> taskClient = new TaskClientImpl<>(GA_QUEUE);

		Tsp tsp = parameter.getTsp();
		int populationSize = parameter.getPopulationSize();
		int maxIterations = parameter.getMaxIterations();
		DistancesGlobal.setDistances(tsp.getDistances());

		int citySize = tsp.getCities().length;

		// Init population
		int[][] population = null;
		int persitedIteration = 0;
		SolverData<?> solverData = solverService.getSolverData(solverId);
		if (solverData != null) {
			population = convertToArray(solverData.getData());
			persitedIteration = solverData.getIteration();
			LOG.info("Resuming from iteration {}", persitedIteration);
		} else {
			population = initPopulation(populationSize, citySize);
		}

		boolean end = false;
		int iteration = 0;
		long startTime = System.currentTimeMillis();

		while (!end) {
			// recombination
			int[][] offsprings = new int[populationSize][citySize];
			for (int i = 0; i < populationSize; i++) {
				int indexParent1 = rand.nextInt(populationSize);
				int indexParent2 = rand.nextInt(populationSize);

				offsprings[i] = recombination(population[indexParent1],
						population[indexParent2]);
			}

			// mutation
			int[][] mutated = new int[populationSize][citySize];
			for (int i = 0; i < populationSize; i++) {
				mutated[i] = mutation(offsprings[i]);
			}

			// evaluation
			double[] values = new double[populationSize * 2];
			try {
				List<TaskFuture<Double>> taskFutures = taskClient
						.addAll(population);

				for (int i = 0; i < populationSize; i++) {
					taskFutures.add(taskClient.add(mutated[i]));
				}

				for (int i = 0; i < taskFutures.size(); i++) {
					values[i] = taskFutures.get(i).get();
				}
			} catch (InterruptedException e) {
				LOG.error("Error while remote task computation", e);
				throw new AlgorithmException(e);
			}

			// selection
			for (int i = 0; i < populationSize * 2; i++) {
				for (int j = i + 1; j < populationSize * 2; j++) {
					if (values[j] < values[i]) {
						double tmp = values[j];
						values[j] = values[i];
						values[i] = tmp;

						int[] tmpGenome;
						if (i < populationSize && j < populationSize) {
							// i and j refer to population
							tmpGenome = population[i];
							population[i] = population[j];
							population[j] = tmpGenome;
						} else if (i >= populationSize && j < populationSize) {
							// i refer to mutated, j refer to population
							tmpGenome = mutated[i - populationSize];
							mutated[i - populationSize] = population[j];
							population[j] = tmpGenome;
						} else if (i < populationSize && j >= populationSize) {
							// i refer to population, j refer to mutated
							tmpGenome = population[i];
							population[i] = mutated[j - populationSize];
							mutated[j - populationSize] = tmpGenome;
						} else {
							// i and j refer to mutated
							tmpGenome = mutated[i - populationSize];
							mutated[i - populationSize] = mutated[j
									- populationSize];
							mutated[j - populationSize] = tmpGenome;
						}
					}
				}
			}

			iteration++;

			solverData = new SolverData<int[][]>(population, values[0],
					iteration + persitedIteration);
			solverService.setSolverData(solverId, solverData);
			// TODO read data back in, becaus possibly there is some change
			// coming from islands

			if (iteration == maxIterations) {
				end = true;
			}
			if (iteration % logSteps == 0 || iteration < 10) {
				long endTime = System.currentTimeMillis();
				LOG.info(
						"Counter: {} ({} worker computations) | last {} GA iterations took {} ms",
						iteration, 2 * iteration * populationSize, logSteps,
						endTime - startTime);
				startTime = endTime;
				printGenome(tsp.getDistances(), population[0], citySize);
			}

			solverService.setProgress(solverId, (float) iteration
					/ (float) maxIterations);
		}

		return population[0];
	}

	private int[][] convertToArray(Object input) {
		@SuppressWarnings("unchecked")
		List<List<Integer>> data = (List<List<Integer>>) input;
		int length1 = data.size();
		int length2 = length1 == 0 ? 0 : data.get(0).size();
		int[][] population = new int[length1][length2];

		for (int i = 0; i < length1; i++) {
			for (int j = 0; j < length2; j++) {
				population[i][j] = data.get(i).get(j);
			}
		}

		return population;
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
		LOG.info("fitness: {} | {}",
				GaFitness.computeFitness(distances, solution), sb.toString());
	}

}
