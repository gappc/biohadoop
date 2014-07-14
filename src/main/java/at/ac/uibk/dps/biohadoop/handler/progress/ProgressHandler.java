package at.ac.uibk.dps.biohadoop.handler.progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.datastore.DataClient;
import at.ac.uibk.dps.biohadoop.datastore.DataClientImpl;
import at.ac.uibk.dps.biohadoop.datastore.DataOptions;
import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerConstants;
import at.ac.uibk.dps.biohadoop.handler.HandlerInitException;
import at.ac.uibk.dps.biohadoop.solver.SolverId;
import at.ac.uibk.dps.biohadoop.solver.SolverService;

public class ProgressHandler implements Handler {

	private static final Logger LOG = LoggerFactory
			.getLogger(ProgressHandler.class);

	private SolverId solverId;

	@Override
	public void init(SolverId solverId) throws HandlerInitException {
		this.solverId = solverId;
	}

	@Override
	public void update(String operation) {
		if (HandlerConstants.DEFAULT.equals(operation)) {
			DataClient dataClient = new DataClientImpl(solverId);

			try {
				Integer maxIterations = dataClient
						.getData(DataOptions.MAX_ITERATIONS);
				Integer iteration = dataClient
						.getData(DataOptions.ITERATION_STEP);
				SolverService.getInstance().setProgress(solverId,
						(float) iteration / (float) maxIterations);
			} catch (Exception e) {
				LOG.error("Could not compute progress, setting it to -1", e);
				SolverService.getInstance().setProgress(solverId, -1);
			}
		}
	}

}
