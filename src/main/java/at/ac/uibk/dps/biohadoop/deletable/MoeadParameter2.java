package at.ac.uibk.dps.biohadoop.deletable;

public class MoeadParameter2 {

	private final int maxIterations;
	private final int N;
	private final int neighborSize;
	private final int genomeSize;

	public MoeadParameter2(int maxIterations, int n, int neighborSize,
			int genomeSize) {
		this.maxIterations = maxIterations;
		N = n;
		this.neighborSize = neighborSize;
		this.genomeSize = genomeSize;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public int getN() {
		return N;
	}

	public int getNeighborSize() {
		return neighborSize;
	}

	public int getGenomeSize() {
		return genomeSize;
	}
}
