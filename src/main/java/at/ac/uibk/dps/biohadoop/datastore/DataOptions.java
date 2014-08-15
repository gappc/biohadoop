package at.ac.uibk.dps.biohadoop.datastore;

import java.util.TimeZone;

import org.xnio.Option;
import org.xnio.Options;

import at.ac.uibk.dps.biohadoop.solver.SolverData;

public final class DataOptions {

	public static final Option<SolverData> SOLVER_DATA = Option.simple(
			Options.class, "SOLVER_DATA", SolverData.class);
	public static final Option<Boolean> COMPUTATION_RESUMED = Option.simple(
			Options.class, "COMPUTATION_RESUMED", Boolean.class);
	public static final Option<Object> DATA = Option.simple(Options.class,
			"DATA", Object.class);
	public static final Option<Double> FITNESS = Option.simple(Options.class,
			"FITNESS", Double.class);
	public static final Option<Integer> ITERATION_START = Option.simple(
			Options.class, "ITERATION_START", Integer.class);
	public static final Option<Integer> ITERATION_STEP = Option.simple(
			Options.class, "ITERATION_STEP", Integer.class);
	public static final Option<Integer> MAX_ITERATIONS = Option.simple(
			Options.class, "MAX_ITERATIONS", Integer.class);
	public static final Option<Long> TIMESTAMP = Option.simple(Options.class,
			"TIMESTAMP", Long.class);
	public static final Option<TimeZone> TIMEZONE = Option.simple(
			Options.class, "TIMEZONE", TimeZone.class);
	
	private DataOptions() {
	}

}
