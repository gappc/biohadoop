package at.ac.uibk.dps.biohadoop.deletable.service.job.api;

public enum JobState {
	// Job has yet not inserted any task to queue
	NEW,
	// Job has inserted all tasks to task queue, some tasks may already be
	// running or are even finished
	SUBMITTED,
	// Tasks are running
	RUNNING,
	// All tasks are finished
	FINISHED,
	// There was some error
	ERROR
}
