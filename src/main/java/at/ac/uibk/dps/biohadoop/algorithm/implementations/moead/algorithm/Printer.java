package at.ac.uibk.dps.biohadoop.algorithm.implementations.moead.algorithm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Printer {

	private static final Logger LOG = LoggerFactory.getLogger(Printer.class);

	private Printer() {
	}

	public static void printWeightVectors(double[][] weightVectors) {
		LOG.info("-----Weight vectors-----");
		printDoubles(weightVectors);
		LOG.info("------------------------");
	}

	public static void printNeighbors(int[][] neighbors) {
		LOG.info("-----Neighbors-----");
		printInts(neighbors);
		LOG.info("-------------------");
	}

	public static void printPopulation(double[][] population) {
		LOG.info("-----Population-----");
		printDoubles(population);
		LOG.info("--------------------");
	}

	public static void printSolution(List<List<Double>> solution) {
		LOG.info("-----Solution-----");
		for (List<Double> l : solution) {
			LOG.info("{}", l);
		}
		LOG.info("------------------");
	}

	private static void printDoubles(double[][] values) {
		for (int i = 0; i < values.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < values[i].length; j++) {
				sb.append(values[i][j] + ", ");
			}
			LOG.info(sb.toString());
		}
	}

	private static void printInts(int[][] values) {
		for (int i = 0; i < values.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < values[i].length; j++) {
				sb.append(values[i][j] + ", ");
			}
			LOG.info(sb.toString());
		}
	}
}
