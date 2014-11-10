package at.ac.uibk.dps.biohadoop.tasksystem.algorithm;

public class AlgorithmInfo {

	private final AlgorithmConfiguration algorithmConfig;
	private float progress;

	public AlgorithmInfo(AlgorithmConfiguration algorithmConfig) {
		this.algorithmConfig = algorithmConfig;
	}

	public AlgorithmConfiguration getAlgorithmConfiguration() {
		return algorithmConfig;
	}

	public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

}
