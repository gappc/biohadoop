package at.ac.uibk.dps.biohadoop.solver.nsgaii.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.config.Algorithm;
import at.ac.uibk.dps.biohadoop.config.AlgorithmException;
import at.ac.uibk.dps.biohadoop.datastore.DataClient;
import at.ac.uibk.dps.biohadoop.datastore.DataClientImpl;
import at.ac.uibk.dps.biohadoop.datastore.DataOptions;
import at.ac.uibk.dps.biohadoop.handler.HandlerClient;
import at.ac.uibk.dps.biohadoop.handler.HandlerClientImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskClient;
import at.ac.uibk.dps.biohadoop.queue.TaskClientImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskFuture;
import at.ac.uibk.dps.biohadoop.service.solver.SolverId;
import at.ac.uibk.dps.biohadoop.solver.nsgaii.config.NsgaIIAlgorithmConfig;

public class NsgaII implements Algorithm<NsgaIIAlgorithmConfig, double[][]> {

	public static final String NSGAII_QUEUE = "NSGAII_QUEUE";

	private static final Logger LOG = LoggerFactory.getLogger(NsgaII.class);
	private static final int LOG_STEPS = 100;

	private TaskClient<double[], double[]> taskClient = new TaskClientImpl<>(
			NSGAII_QUEUE);

	@Override
	public double[][] compute(SolverId solverId, NsgaIIAlgorithmConfig config)
			throws AlgorithmException {
		HandlerClient handlerClient = new HandlerClientImpl(solverId);
		DataClient dataClient = new DataClientImpl(solverId);

		int genomeSize = config.getGenomeSize();
		int maxIterations = config.getMaxIterations();
		int populationSize = config.getPopulationSize();

		long startTime = System.currentTimeMillis();

		double[][] population = null;
		int persitedIteration = 0;
		if (dataClient.getData(DataOptions.COMPUTATION_RESUMED, false)) {
			population = convertToArray(dataClient.getData(DataOptions.DATA));
			persitedIteration = dataClient.getData(DataOptions.ITERATION_START);
			LOG.info("Resuming from iteration {}", persitedIteration);
		} else {
			population = initializePopulation(populationSize * 2, genomeSize);
		}

		double[][] objectiveValues = new double[populationSize * 2][2];
		computeObjectiveValues(population, objectiveValues, 0, populationSize);

		int iteration = 0;
		boolean end = false;
		while (!end) {
			produceOffsprings(population, objectiveValues);
			computeObjectiveValues(population, objectiveValues, populationSize,
					populationSize * 2);

			NondominatedSortResult nondominatedSortResult = fastNondominatedSort(
					population, objectiveValues, populationSize * 2);
			double[] crowdingDistances = crowdingDistance(objectiveValues,
					nondominatedSortResult.getFronts());
			double[][] newPopulation = new double[populationSize * 2][genomeSize];

			int currentRank = 0;
			int newPopSize = 0;
			while (newPopSize < populationSize) {
				List<Integer> front = nondominatedSortResult.getFronts().get(
						currentRank);
				// if there is place enough for a complete rank, just insert it
				if (newPopSize + front.size() <= populationSize) {
					for (int genome : front) {
						newPopulation[newPopSize++] = population[genome];
					}
				}
				// if there is not enough place, use crowding distance to choose
				// best solutions from current front
				else {
					List<TupleSort> tuples = new ArrayList<TupleSort>();
					for (int i = 0; i < front.size(); i++) {
						int index = front.get(i);
						TupleSort tuple = new TupleSort(
								crowdingDistances[index], index);
						tuples.add(tuple);
					}
					Collections.sort(tuples);
					Collections.reverse(tuples);

					int missingElements = populationSize - newPopSize;
					for (int i = 0; i < missingElements; i++) {
						newPopulation[newPopSize++] = population[tuples.get(i)
								.getPos()];
					}
				}
				currentRank++;
			}

			population = newPopulation;

			computeObjectiveValues(population, objectiveValues, 0,
					populationSize);

			iteration++;

			double[][] result = computeResult(populationSize, objectiveValues);

			dataClient.setDefaultData(result, 0, maxIterations, iteration);
			handlerClient.invokeDefaultHandlers();
			objectiveValues = (double[][]) dataClient.getData(DataOptions.DATA,
					objectiveValues);

			if (iteration >= maxIterations) {
				end = true;
			}
			if (iteration % 100 == 0) {
				long endTime = System.currentTimeMillis();
				LOG.info("Counter: {} | last {} NSGAII iterations took {} ms",
						iteration + persitedIteration, LOG_STEPS, endTime
								- startTime);
				startTime = endTime;
			}
		}

		return computeResult(populationSize, objectiveValues);
	}

	private double[][] convertToArray(Object input) {
		@SuppressWarnings("unchecked")
		List<List<Double>> data = (List<List<Double>>) input;
		int length1 = data.size();
		int length2 = length1 == 0 ? 0 : data.get(0).size();
		double[][] population = new double[length1 * 2][length2];

		for (int i = 0; i < length1; i++) {
			for (int j = 0; j < length2; j++) {
				population[i][j] = data.get(i).get(j);
			}
		}

		return population;
	}

	// Initialize population, where first half (of size populationSize) is
	// filled with random numbers, and second half is initialized with
	// zeros. second half contains offsprings
	private double[][] initializePopulation(int populationSize, int genomeSize) {
		Random rand = new Random();
		double[][] population = new double[populationSize][genomeSize];
		for (int i = 0; i < populationSize / 2; i++) {
			for (int j = 0; j < genomeSize; j++) {
				population[i][j] = rand.nextDouble();
			}
		}
		return population;
	}

	private void produceOffsprings(double[][] population,
			double[][] objectiveValues) {
		Random rand = new Random();
		int populationSize = population.length / 2;
		int genomeSize = population[0].length;

		NondominatedSortResult nondominatedSortedFront = fastNondominatedSort(
				population, objectiveValues, populationSize);
		double[] crowdingDistances = crowdingDistance(objectiveValues,
				nondominatedSortedFront.getFronts());

		for (int i = 0; i < populationSize; i++) {
			// int[] parents = parentSelectionRandom(populationSize);
			int[] parents = parentSelectionTournament(populationSize,
					nondominatedSortedFront, crowdingDistances);
			for (int j = 0; j < genomeSize; j++) {
				population[i + populationSize][j] = (population[parents[0]][j] + population[parents[1]][j]) / 2.0;
			}

			// mutate; parameters taken from
			// http://repository.ias.ac.in/83498/1/2-a.pdf, page 8
			if (rand.nextDouble() > 1 / populationSize) {
				for (int j = 0; j < populationSize / 30; j++) {
					int pos = rand.nextInt(genomeSize);
					population[i + populationSize][pos] = rand.nextDouble();
				}
			}
		}
	}

	// private int[] parentSelectionRandom(int populationSize) {
	// Random rand = new Random();
	// int[] parents = new int[2];
	// parents[0] = rand.nextInt(populationSize);
	// parents[1] = rand.nextInt(populationSize);
	// return parents;
	// }

	private int[] parentSelectionTournament(final int populationSize,
			final NondominatedSortResult nondominatedSortedFront,
			final double[] crowdingDistances) {
		Random rand = new Random();
		int[] parents = new int[2];

		// increase pool size to get from binary tournament to higher order
		int poolSize = 2;
		Integer[] parentPool = new Integer[poolSize];

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < poolSize; j++) {
				parentPool[j] = rand.nextInt(populationSize);
			}

			Arrays.sort(parentPool, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					int ranking1 = nondominatedSortedFront.getElementsRanking()[o1];
					int ranking2 = nondominatedSortedFront.getElementsRanking()[o2];

					if (ranking1 != ranking2) {
						return ranking1 - ranking2;
					}

					return new Double(Math.signum(crowdingDistances[o1]
							- crowdingDistances[o2])).intValue();
				}
			});
			parents[i] = parentPool[0];
		}
		return parents;
	}

	// taken from http://repository.ias.ac.in/83498/1/2-a.pdf
	private NondominatedSortResult fastNondominatedSort(double[][] population,
			double[][] objectiveValues, int size) {
		int[] elementRanking = new int[size];

		List<List<Integer>> S = new ArrayList<List<Integer>>();
		List<List<Integer>> frontRanking = new ArrayList<List<Integer>>();
		for (int i = 0; i < size + 1; i++) {
			S.add(new ArrayList<Integer>());
			frontRanking.add(new ArrayList<Integer>());
		}

		int[] n = new int[size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				// if p (= combinedPopulation[i]) dominates q (=
				// combinedPopulation[j]) then include q in Sp (which is S[i])
				if (dominates(objectiveValues[i], objectiveValues[j])) {
					// solution i dominates solution j
					S.get(i).add(j);
				}
				// if p is dominated by q then increment np (which is n[i])
				else if (dominates(objectiveValues[j], objectiveValues[i])) {
					// solution i is dominated by an additional other solution
					n[i]++;
				}
			}
			// if no solution dominates p then p is a member of the first front
			if (n[i] == 0) {
				frontRanking.get(0).add(i);
			}
		}

		int i = 0;
		while (!frontRanking.get(i).isEmpty()) {
			List<Integer> H = new ArrayList<Integer>();
			for (int memberP : frontRanking.get(i)) {
				for (int memberQ : S.get(memberP)) {
					n[memberQ]--;
					if (n[memberQ] == 0) {
						H.add(memberQ);
					}
				}
			}
			i++;
			frontRanking.set(i, H);
			for (int index : H) {
				elementRanking[index] = i;
			}
		}
		return new NondominatedSortResult(frontRanking, elementRanking);
	}

	private double[][] computeObjectiveValues(double[][] population,
			double[][] objectiveValues, int start, int end)
			throws AlgorithmException {
		try {
			List<TaskFuture<double[]>> taskFutures = taskClient
					.addAll(population);
			for (int i = 0; i < taskFutures.size(); i++) {
				objectiveValues[i] = taskFutures.get(i).get();
			}
			return objectiveValues;
		} catch (InterruptedException e) {
			LOG.error("Error while remote task computation", e);
			throw new AlgorithmException(e);
		}
	}

	private boolean dominates(double[] ds, double[] ds2) {
		boolean lesser = false;
		for (int i = 0; i < ds.length; i++) {
			if (ds[i] > ds2[i]) {
				return false;
			}
			if (ds[i] < ds2[i]) {
				lesser = true;
			}
		}
		return lesser;
	}

	private double[] crowdingDistance(double[][] objectiveValues,
			List<List<Integer>> frontRanking) {

		double[] result = new double[objectiveValues.length];

		for (List<Integer> front : frontRanking) {

			int l = front.size();
			if (l > 0) {
				// m is objective
				for (int m = 0; m < objectiveValues[0].length; m++) {
					int[] sorted = sortAccordingObjective(objectiveValues,
							front, m);
					result[sorted[0]] = Double.MAX_VALUE;
					result[sorted[l - 1]] = Double.MAX_VALUE;

					// double fmin = objectiveValues[sorted[0]][m];
					// double fmax = objectiveValues[sorted[l - 1]][m];

					for (int i = 1; i < l - 1; i++) {
						result[sorted[i]] += (objectiveValues[sorted[i + 1]][m] - objectiveValues[sorted[i - 1]][m]);
						// / (fmax - fmin);
					}
				}
			} else {
				break;
			}
		}

		return result;
	}

	private int[] sortAccordingObjective(double[][] objectiveValues,
			List<Integer> front, int m) {
		int[] result = new int[front.size()];

		List<TupleSort> objectiveSorts = new ArrayList<TupleSort>();
		for (int i = 0; i < front.size(); i++) {
			objectiveSorts.add(new TupleSort(objectiveValues[i][m], front
					.get(i)));
		}

		Collections.sort(objectiveSorts);

		for (int i = 0; i < front.size(); i++) {
			result[i] = objectiveSorts.get(i).getPos();
		}

		return result;
	}

	private double[][] computeResult(int populationSize,
			double[][] objectiveValues) {
		// normalize
		double minF1 = Double.MAX_VALUE;
		double maxF1 = -Double.MAX_VALUE;
		double minF2 = Double.MAX_VALUE;
		double maxF2 = -Double.MAX_VALUE;
		for (int i = 0; i < populationSize; i++) {
			if (objectiveValues[i][0] < minF1) {
				minF1 = objectiveValues[i][0];
			}
			if (objectiveValues[i][0] > maxF1) {
				maxF1 = objectiveValues[i][0];
			}
			if (objectiveValues[i][1] < minF2) {
				minF2 = objectiveValues[i][1];
			}
			if (objectiveValues[i][1] > maxF2) {
				maxF2 = objectiveValues[i][1];
			}
		}

		double[][] result = new double[objectiveValues.length][objectiveValues[0].length];
		for (int i = 0; i < populationSize; i++) {
			result[i][0] = (objectiveValues[i][0] - minF1) / (maxF1 - minF1);
			result[i][1] = (objectiveValues[i][1] - minF2) / (maxF2 - minF2);
		}
		return result;
	}

	private class TupleSort implements Comparable<TupleSort> {
		double value;
		int pos;

		public TupleSort(double value, int pos) {
			super();
			this.value = value;
			this.pos = pos;
		}

		public double getValue() {
			return value;
		}

		public int getPos() {
			return pos;
		}

		@Override
		public int compareTo(TupleSort c) {
			double o = c.getValue();
			return value == o ? 0 : value < o ? -1 : 1;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TupleSort)) {
				return false;
			}
			TupleSort tupleSort = (TupleSort) obj;
			return this.value == tupleSort.value && this.pos == tupleSort.pos;
		}

		@Override
		public int hashCode() {
			long longTmp = Double.doubleToLongBits(value);
			int intTmp = (int) (longTmp ^ (longTmp >>> 32));
			return intTmp * 13 + pos * 17;
		}

		@Override
		public String toString() {
			return pos + "|" + value;
		}

	}

}