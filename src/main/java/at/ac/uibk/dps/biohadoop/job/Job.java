package at.ac.uibk.dps.biohadoop.job;

public class Job {

	private Task task;
	private TaskState taskState;
	private long created = System.currentTimeMillis();
	
	public Job(Task task, TaskState taskState) {
		super();
		this.task = task;
		this.taskState = taskState;
	}

	public TaskState getTaskState() {
		return taskState;
	}

	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}

	public Task getTask() {
		return task;
	}

	public long getCreated() {
		return created;
	}
	
}
