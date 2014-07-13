package at.ac.uibk.dps.biohadoop.solver.ga.algorithm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;

public class TspFileReader {

	private static final Logger LOG = LoggerFactory
			.getLogger(TspFileReader.class);

	private TspFileReader() {
	}
	
	public static Tsp readFile(final String datafile) throws IOException {
		InputStream is = HdfsUtil.openFile(new YarnConfiguration(), datafile);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = br.readLine()) != null) {
			lines.add(line);
		}
		return readFile(lines);
	}

	private static Tsp readFile(final List<String> lines) {
		List<double[]> citiesList = new ArrayList<double[]>();
		for (String line : lines) {
			try {
				double[] city = new double[2];
				String[] tokens = line.split(" ");
				city[0] = Double.parseDouble(tokens[1]);
				city[1] = Double.parseDouble(tokens[2]);
				citiesList.add(city);
			} catch (Exception e) {
				LOG.debug("Line contains not a valid city: {}", line, e);
			}
		}

		double[][] cities = getCitiesAsArray(citiesList);
		double[][] distances = getDistances(cities);

		Tsp tsp = new Tsp();
		tsp.setCities(cities);
		tsp.setDistances(distances);

		return tsp;
	}

	private static double[][] getCitiesAsArray(List<double[]> cities) {
		return cities.toArray(new double[cities.size()][2]);
	}

	private static double[][] getDistances(double[][] cities) {
		double[][] distances = new double[cities.length][cities.length];

		for (int i = 0; i < cities.length; i++) {
			for (int j = i; j < cities.length; j++) {
				double distance = Math.sqrt(Math.pow(cities[i][0]
						- cities[j][0], 2)
						+ Math.pow(cities[i][1] - cities[j][1], 2));
				distances[i][j] = distance;
				distances[j][i] = distance;
			}
		}

		return distances;
	}
}
