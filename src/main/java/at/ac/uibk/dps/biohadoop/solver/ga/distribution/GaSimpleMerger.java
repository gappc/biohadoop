package at.ac.uibk.dps.biohadoop.solver.ga.distribution;

import at.ac.uibk.dps.biohadoop.service.distribution.DataMergeException;
import at.ac.uibk.dps.biohadoop.service.distribution.DataMerger;

public class GaSimpleMerger implements DataMerger<int[][]> {

	@Override
	public int[][] merge(int[][] o1, int[][] o2) throws DataMergeException {
		if (o1 == null || o2 == null) {
			throw new DataMergeException(
					"Could not merge data because at least one of merging objects was null: o1="
							+ o1 + ", o2=" + o2);
		}

		int length1 = o1.length;
		int length2 = length1 > 0 ? o1[0].length : 0;
		int[][] result = new int[length1][length2];

		int halfSize = length1 / 2;
		for (int i = 0; i < halfSize; i++) {
			for (int j = 0; j < length2; j++) {
				result[i][j] = o1[i][j];
			}
		}
		for (int i = halfSize; i < length1; i++) {
			for (int j = 0; j < length2; j++) {
				result[i][j] = o2[i][j];
			}
		}
		return result;
	}

}
