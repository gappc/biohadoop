package at.ac.uibk.dps.biohadoop.ga.config;

import java.io.IOException;

import at.ac.uibk.dps.biohadoop.config.AlgorithmConfiguration;
import at.ac.uibk.dps.biohadoop.config.BuildParameterException;
import at.ac.uibk.dps.biohadoop.ga.algorithm.Tsp;
import at.ac.uibk.dps.biohadoop.ga.algorithm.TspFileReader;

public class GaAlgorithmConfig implements AlgorithmConfiguration {

	private String dataFile;
	private int populationSize;
	private int maxIterations;

	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	@Override
	public Object buildParameters() throws BuildParameterException {
		Tsp tsp;
		try {
			tsp = TspFileReader.readFile(dataFile);
			return new GaParameter(tsp, populationSize, maxIterations);
		} catch (IOException e) {
			throw new BuildParameterException("Could not read file " + dataFile);
		}
	}

}
