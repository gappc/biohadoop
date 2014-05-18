package at.ac.uibk.dps.biohadoop.nsgaii.algorithm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NsgaII {

	private static final Logger LOG = LoggerFactory.getLogger(NsgaII.class);
	private int logSteps = 100;

	public void run(int maxIterations, int populationSize, int genomeSize) {
		long startTime = System.currentTimeMillis();

		// Initialize population, where first half (of size populationSize) is
		// filled with random numbers, and second half is initialized with
		// zeros. second half contains offsprings
		double[][] population = initializePopulation(populationSize * 2,
				genomeSize);

		double[][] functionValues = new double[populationSize * 2][2];
		computeFunctionValues(population, functionValues, 0, populationSize);

		int counter = 0;
		boolean end = false;
		while (!end) {
			produceOffsprings(population);
			computeFunctionValues(population, functionValues, populationSize,
					populationSize * 2);

			List<List<Integer>> nondominatedSortedFront = fastNondominatedSort(
					population, functionValues);
			List<List<Double>> crowdingDistances = crowdingDistance(population,
					nondominatedSortedFront);
			double[][] newPopulation = new double[populationSize * 2][genomeSize];

			int currentRank = 0;
			int newPopSize = 0;
			while (newPopSize < populationSize) {
				List<Integer> front = nondominatedSortedFront.get(currentRank);
				// System.out.println("newPopSize: " + newPopSize
				// + " currentRank: " + currentRank + " frontSize: "
				// + front.size());
				// if there is place enough for a complete rank, just insert it
				if (newPopSize + front.size() <= populationSize) {
					for (int genome : front) {
						newPopulation[newPopSize++] = population[genome];
					}
				}
				// if there is not enough place, use crowding distance to choose
				// best solutions from current front
				else {
					List<Double> distances = crowdingDistances.get(currentRank);
					List<TupleSort> tuples = new ArrayList<TupleSort>();
					for (int i = 0; i < distances.size(); i++) {
						tuples.add(new TupleSort(distances.get(i), front.get(i)));
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

			computeFunctionValues(population, functionValues, 0, populationSize);

			counter++;
			if (counter % 100 == 0) {
				long endTime = System.currentTimeMillis();
				LOG.info("Counter: {} | last {} NSGAII iterations took {} ms",
						counter, logSteps, endTime - startTime);
				startTime = endTime;
			}
			if (counter >= maxIterations) {
				end = true;
			}
			System.out.println("\n-------" + counter
					+ "-----------------------------\n");
			// for (int i = 0; i < populationSize; i++) {
			// System.out.println(functionValues[i][0] + " " +
			// functionValues[i][1]);
			// }
			// System.out.println("\n-------" + counter
			// + "-----------------------------\n");
		}

		// normalize
		double minF1 = Double.MAX_VALUE;
		double maxF1 = -Double.MAX_VALUE;
		double minF2 = Double.MAX_VALUE;
		double maxF2 = -Double.MAX_VALUE;
		for (int i = 0; i < populationSize; i++) {
			if (functionValues[i][0] < minF1) {
				minF1 = functionValues[i][0];
			}
			if (functionValues[i][0] > maxF1) {
				maxF1 = functionValues[i][0];
			}
			if (functionValues[i][1] < minF2) {
				minF2 = functionValues[i][1];
			}
			if (functionValues[i][1] > maxF2) {
				maxF2 = functionValues[i][1];
			}
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(
				"/tmp/nsgaii-sol.txt"))) {
			for (int i = 0; i < populationSize; i++) {
				String output = ((functionValues[i][0] - minF1) / (maxF1 - minF1))
						+ " "
						+ ((functionValues[i][1] - minF2) / (maxF2 - minF2));
				System.out.println(output);
				bw.write(output + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// no binary tournament selection for parents implemented (would need
	// information about rank and crowding distance). Instead, random parent
	// selection is used
	private void produceOffsprings(double[][] population) {
		Random rand = new Random();
		int populationSize = population.length / 2;
		int genomeSize = population[0].length;
		for (int i = 0; i < populationSize; i++) {

			// recombine
			int parent1 = rand.nextInt(populationSize);
			int parent2 = rand.nextInt(populationSize);
			for (int j = 0; j < genomeSize; j++) {
				population[i + populationSize][j] = (population[parent1][j] + population[parent2][j]) / 2.0;
			}

			// mutate; parameters taken from
			// http://repository.ias.ac.in/83498/1/2-a.pdf, page 8
			if (rand.nextDouble() > 1 / populationSize) {
				int pos = rand.nextInt(genomeSize);
				population[i + populationSize][pos] = rand.nextDouble();
			}
		}
	}

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

	// public static void main(String[] args) {
	// NsgaII nsgaII = new NsgaII();
	// double[] A = new double[] { 1.0, 10.0 };
	// double[] A1 = new double[] { 1.0, 7.0 };
	// double[] B = new double[] { 3.0, 2.0 };
	// double[] C = new double[] { 2.0, 3.0 };
	// double[] D = new double[] { 4.0, 5.0 };
	// double[] E = new double[] { 5.0, 4.0 };
	// System.out.println(nsgaII.dominates(A, D));
	// System.out.println(nsgaII.dominates(B, D));
	//
	// double[][] all = new double[][] { A, B, C, D, E };
	//
	// List<List<Integer>> frontRanking = nsgaII.fastNondominatedSort(all);
	// System.out.println(frontRanking);
	//
	// List<List<Double>> distances = nsgaII.crowdingDistanceAssignment(all,
	// frontRanking);
	// System.out.println(distances);
	// }

	// taken from http://repository.ias.ac.in/83498/1/2-a.pdf
	private List<List<Integer>> fastNondominatedSort(double[][] population,
			double[][] functionValues) {
		// double[][] functionValues =
		// computeFunctionValues(combinedPopulation);
		// double[][] functionValues = combinedPopulation;

		int popSize = population.length;
		List<List<Integer>> S = new ArrayList<List<Integer>>();
		List<List<Integer>> frontRanking = new ArrayList<List<Integer>>();
		for (int i = 0; i < popSize; i++) {
			S.add(new ArrayList<Integer>());
			frontRanking.add(new ArrayList<Integer>());
		}

		int[] n = new int[popSize];

		for (int i = 0; i < popSize; i++) {
			for (int j = 0; j < popSize; j++) {
				// if p (= combinedPopulation[i]) dominates q (=
				// combinedPopulation[j]) then include q in Sp (which is S[i])
				if (dominates(functionValues[i], functionValues[j])) {
					// solution i dominates solution j
					S.get(i).add(j);
				}
				// if p is dominated by q then increment np (which is n[i])
				else if (dominates(functionValues[j], functionValues[i])) {
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
		while (frontRanking.get(i).size() > 0) {
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
		}
		return frontRanking;
	}

	private double[][] computeFunctionValues(double[][] population,
			double[][] functionValues, int start, int end) {
		for (int i = start; i < end; i++) {
			functionValues[i][0] = Functions.f1(population[i]);
			functionValues[i][1] = Functions.f2(population[i]);
		}
		return functionValues;
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

	private List<List<Double>> crowdingDistance(double[][] functionValue,
			List<List<Integer>> frontRanking) {
		List<List<Double>> result = new ArrayList<List<Double>>();
		for (List<Integer> front : frontRanking) {
			List<Double> distances = new ArrayList<Double>();
			result.add(distances);

			int l = front.size();
			if (l > 0) {
				double[] distance = new double[l];
				// m is objective
				for (int m = 0; m < functionValue[0].length; m++) {
					int[] sorted = sortAccordingObjective(functionValue, front,
							m);
					distance[0] = Double.MAX_VALUE;
					distance[l - 1] = Double.MAX_VALUE;

					double fmin = functionValue[sorted[0]][m];
					double fmax = functionValue[sorted[l - 1]][m];

					for (int i = 1; i < l - 1; i++) {
						distance[i] += (functionValue[sorted[i + 1]][m] - functionValue[sorted[i - 1]][m])
								/ (fmax - fmin);
					}
				}
				for (double d : distance) {
					distances.add(d);
				}
			}
		}

		return result;
	}

	private int[] sortAccordingObjective(double[][] functionValue,
			List<Integer> front, int m) {
		int[] result = new int[front.size()];

		List<TupleSort> objectiveSorts = new ArrayList<TupleSort>();
		for (int i = 0; i < front.size(); i++) {
			objectiveSorts
					.add(new TupleSort(functionValue[i][m], front.get(i)));
		}

		Collections.sort(objectiveSorts);

		for (int i = 0; i < front.size(); i++) {
			result[i] = objectiveSorts.get(i).getPos();
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
		public String toString() {
			return pos + "|" + value;
		}

	}

}