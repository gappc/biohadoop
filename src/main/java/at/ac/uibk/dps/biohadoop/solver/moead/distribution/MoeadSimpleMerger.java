package at.ac.uibk.dps.biohadoop.solver.moead.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.distribution.DataMergeException;
import at.ac.uibk.dps.biohadoop.service.distribution.DataMerger;

public class MoeadSimpleMerger implements DataMerger<double[][]> {

	private static final Logger LOG = LoggerFactory
			.getLogger(MoeadSimpleMerger.class);

	@Override
	public double[][] merge(double[][] o1, double[][] o2) throws DataMergeException {
		if (o1 == null || o2 == null) {
			throw new DataMergeException(
					"Could not merge data because at least one of merging objects was null: o1="
							+ o1 + ", o2=" + o2);
		}

		LOG.error("No merging defined, returning same data");
		return o1;
	}

}
