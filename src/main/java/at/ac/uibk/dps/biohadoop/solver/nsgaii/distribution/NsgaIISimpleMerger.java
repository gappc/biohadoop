package at.ac.uibk.dps.biohadoop.solver.nsgaii.distribution;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.service.distribution.DataMergeException;
import at.ac.uibk.dps.biohadoop.service.distribution.DataMerger;

public class NsgaIISimpleMerger implements DataMerger {

	private final static Logger LOG = LoggerFactory.getLogger(NsgaIISimpleMerger.class);
	
	@Override
	public Object merge(Object o1, Object o2) throws DataMergeException {
		if (o1 == null || o2 == null) {
			throw new DataMergeException(
					"Could not merge data because at least one of merging objects was null: o1="
							+ o1 + ", o2=" + o2);
		}
		if (o1.getClass() != o2.getClass()) {
			throw new DataMergeException(
					"Could not merge data because data types of merging objects are different, o1="
							+ o1.getClass().getCanonicalName() + " o2="
							+ o2.getClass().getCanonicalName());
		}

		LOG.error("No merging defined, returning same data");
		return o1;
	}

}
