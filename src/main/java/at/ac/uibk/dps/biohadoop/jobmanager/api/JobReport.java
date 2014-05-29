package at.ac.uibk.dps.biohadoop.jobmanager.api;

public class JobReport {

	private final JobState state;

	public JobReport(JobState state) {
		this.state = state;
	}

	public JobState getState() {
		return state;
	}

	public boolean isFinished() {
		return state == JobState.FINISHED;
	}

	@Override
	public String toString() {
		return state.toString();
	}
}
