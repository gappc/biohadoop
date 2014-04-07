package at.ac.uibk.dps.biohadoop.ga;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileInput {

	public Tsp readFile(String pathname) throws IOException {
		Path path = Paths.get(pathname);
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

		List<double[]> citiesList = new ArrayList<double[]>();
		for (String line : lines) {
			try {
				double[] city = new double[2];
				String[] tokens = line.split(" ");
				city[0] = Double.parseDouble(tokens[1]);
				city[1] = Double.parseDouble(tokens[2]);
				citiesList.add(city);
			} catch (Exception e) {
				System.out.println("Line contains not a valid city: " + line);
			}
		}

		double[][] cities = getCitiesAsArray(citiesList);
		double[][] distances = getDistances(cities);

		Tsp tsp = new Tsp();
		tsp.setCities(cities);
		tsp.setDistances(distances);
		
		return tsp;
	}

	private double[][] getCitiesAsArray(List<double[]> cities) {
		return cities.toArray(new double[cities.size()][2]);
	}

	private double[][] getDistances(double[][] cities) {
		double[][] distances = new double[cities.length][cities.length];
		
		for (int i = 0; i < cities.length; i++) {
			for (int j = i; j < cities.length; j++) {
				double distance = Math.sqrt(Math.pow(cities[i][0] - cities[j][0], 2) + Math.pow(cities[i][1] - cities[j][1], 2));
				distances[i][j] = distance;
				distances[j][i] = distance;
			}
		}
		
		return distances;
	}
}
