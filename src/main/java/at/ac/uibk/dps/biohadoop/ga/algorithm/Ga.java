package at.ac.uibk.dps.biohadoop.ga.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationData;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationId;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationState;
import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.config.AlgorithmException;
import at.ac.uibk.dps.biohadoop.distributionmanager.DistributionManager;
import at.ac.uibk.dps.biohadoop.ga.DistancesGlobal;
import at.ac.uibk.dps.biohadoop.ga.config.GaParameter;
import at.ac.uibk.dps.biohadoop.jobmanager.JobId;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobRequest;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobRequestData;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponse;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponseData;
import at.ac.uibk.dps.biohadoop.jobmanager.handler.SimpleJobHandler;

public class Ga extends SimpleJobHandler<int[]> implements Algorithm<int[], GaParameter> {

	private static final Logger LOG = LoggerFactory.getLogger(Ga.class);
	public static final String GA_QUEUE = "GA_QUEUE";

	private CountDownLatch latch;

	private Random rand = new Random();
	private int logSteps = 1000;

	@Override
	public int[] compute(ApplicationId applicationId, GaParameter parameter)
			throws AlgorithmException {
		ApplicationManager applicationManager = ApplicationManager
				.getInstance();
		applicationManager.setApplicationState(applicationId,
				ApplicationState.RUNNING);

		JobManager<int[], Double> jobManager = JobManager.getInstance();

		Tsp tsp = parameter.getTsp();
		int populationSize = parameter.getPopulationSize();
		int maxIterations = parameter.getMaxIterations();
		DistancesGlobal.setDistances(tsp.getDistances());

		int citySize = tsp.getCities().length;

		boolean end = false;
		int counter = 0;

		// Init: Generate random population
		int[][] population = initPopulation(populationSize, citySize);

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
			JobRequest<int[]> jobRequest = new JobRequest<>(this);
			for (int i = 0; i < populationSize; i++) {
				jobRequest.add(new JobRequestData<int[]>(population[i], i));
			}
			for (int i = 0; i < populationSize; i++) {
				jobRequest.add(new JobRequestData<int[]>(mutated[i], i
						+ populationSize));
			}

			latch = new CountDownLatch(1);
			JobId jobId = jobManager.submitJob(jobRequest, GA_QUEUE);
			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new AlgorithmException("Error while waiting for latch", e);
			}

			JobResponse<Double> jobResponse = jobManager.getJobResponse(jobId);
			jobManager.jobCleanup(jobId);
			List<JobResponseData<Double>> results = jobResponse
					.getResponseData();

			double[] values = new double[populationSize * 2];
			for (int i = 0; i < populationSize; i++) {
				values[i] = results.get(i).getData();
			}
			for (int i = 0; i < populationSize; i++) {
				values[i + populationSize] = results.get(i + populationSize)
						.getData();
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

			ApplicationData<int[][]> applicationData = new ApplicationData<int[][]>(
					population);
			applicationManager.setApplicationData(applicationId,
					applicationData, true);

			counter++;
			if (counter == maxIterations) {
				end = true;
			}
			if (counter % logSteps == 0 || counter < 10) {
				long endTime = System.currentTimeMillis();
				LOG.info(
						"Counter: {} ({} worker computations) | last {} GA iterations took {} ms",
						counter, 2 * counter * populationSize, logSteps,
						endTime - startTime);
				startTime = endTime;
				printGenome(tsp.getDistances(), population[0], citySize);

				// get remote data
				ApplicationData<int[][]> remoteData = DistributionManager
						.getInstance().getRemoteApplicationData();
				System.out.println(remoteData);
			}
			
			applicationManager.setProgress(applicationId, (float) counter
					/ (float) maxIterations);
		}

		return population[0];
	}

	@Override
	public void onFinished(JobId jobId) {
		latch.countDown();
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
