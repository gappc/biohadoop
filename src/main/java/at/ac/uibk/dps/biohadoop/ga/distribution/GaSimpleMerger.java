package at.ac.uibk.dps.biohadoop.ga.distribution;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.distributionmanager.DataMergeException;
import at.ac.uibk.dps.biohadoop.distributionmanager.DataMerger;

public class GaSimpleMerger implements DataMerger {

	// @SuppressWarnings("unchecked")
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

		@SuppressWarnings("unchecked")
		List<List<Integer>> p1 = (List<List<Integer>>) o1;
		@SuppressWarnings("unchecked")
		List<List<Integer>> p2 = (List<List<Integer>>) o2;

		List<List<Integer>> result = new ArrayList<>();

		int halfSize = p1.size() / 2;
		for (int i = 0; i < halfSize; i++) {
			List<Integer> resultLine = new ArrayList<>();
			result.add(resultLine);
			for (int j = 0; j < p2.get(i).size(); j++) {
				resultLine.add(p1.get(i).get(j));
			}
		}
		for (int i = halfSize; i < p1.size(); i++) {
			List<Integer> resultLine = new ArrayList<>();
			result.add(resultLine);
			for (int j = 0; j < p2.get(i).size(); j++) {
				resultLine.add(p2.get(i).get(j));
			}
		}
		return result;
	}

}
