package at.ac.uibk.dps.biohadoop.datastore;

import java.util.TimeZone;

import at.ac.uibk.dps.biohadoop.solver.SolverData;

public final class DataOptions {

	public static final Option<SolverData> SOLVER_DATA = new Option(
			"SOLVER_DATA", SolverData.class);
	public static final Option<Boolean> COMPUTATION_RESUMED = new Option(
			"COMPUTATION_RESUMED", Boolean.class);
	public static final Option<Object> DATA = new Option("DATA", Object.class);
	public static final Option<Double> FITNESS = new Option("FITNESS",
			Double.class);
	public static final Option<Integer> ITERATION_START = new Option(
			"ITERATION_START", Integer.class);
	public static final Option<Integer> ITERATION_STEP = new Option(
			"ITERATION_STEP", Integer.class);
	public static final Option<Integer> MAX_ITERATIONS = new Option(
			"MAX_ITERATIONS", Integer.class);
	public static final Option<Long> TIMESTAMP = new Option("TIMESTAMP",
			Long.class);
	public static final Option<TimeZone> TIMEZONE = new Option("TIMEZONE",
			TimeZone.class);

	private DataOptions() {
	}

}
