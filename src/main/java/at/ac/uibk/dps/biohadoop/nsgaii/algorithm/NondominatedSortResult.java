package at.ac.uibk.dps.biohadoop.nsgaii.algorithm;

import java.util.List;

public class NondominatedSortResult {

	private final List<List<Integer>> fronts;
	private final int[] elementsRanking;
	
	public NondominatedSortResult(List<List<Integer>> fronts,
			int[] elementsRanking) {
		super();
		this.fronts = fronts;
		this.elementsRanking = elementsRanking;
	}

	public List<List<Integer>> getFronts() {
		return fronts;
	}

	public int[] getElementsRanking() {
		return elementsRanking;
	}
}
