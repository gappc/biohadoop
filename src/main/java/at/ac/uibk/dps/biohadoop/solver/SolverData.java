package at.ac.uibk.dps.biohadoop.solver;

import java.io.Serializable;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SolverData<T> implements Serializable {

	private static final long serialVersionUID = 1390023331243906792L;

	private final T data;
	private final double fitness;
	private final int iteration;
	private final long timestamp;
	private final TimeZone timezone;

	public SolverData(T data, double fitness, int iteration) {
		this.data = data;
		this.fitness = fitness;
		this.iteration = iteration;
		this.timestamp = System.currentTimeMillis();
		this.timezone = TimeZone.getDefault();
	}

	public SolverData(T data, double fitness, int iteration, long timestamp,
			TimeZone timezone) {
		this.data = data;
		this.fitness = fitness;
		this.iteration = iteration;
		this.timestamp = timestamp;
		this.timezone = timezone;
	}

	@JsonCreator
	public static <T> SolverData<T> create(@JsonProperty("data") T data,
			@JsonProperty("fitness") double fitness,
			@JsonProperty("iteration") int iteration,
			@JsonProperty("timestamp") long timestamp,
			@JsonProperty("timezone") TimeZone timezone) {
		return new SolverData<>(data, fitness, iteration, timestamp, timezone);
	}

	public T getData() {
		return data;
	}

	public int getIteration() {
		return iteration;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public TimeZone getTimezone() {
		return timezone;
	}

	public double getFitness() {
		return fitness;
	}

	@Override
	public String toString() {
		return "Iteration: " + iteration + " | Data: " + data.toString();
	}
}
