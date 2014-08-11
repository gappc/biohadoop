package at.ac.uibk.dps.biohadoop.unifiedcommunication;

import at.ac.uibk.dps.biohadoop.queue.SimpleTask;
import at.ac.uibk.dps.biohadoop.queue.TaskId;

public class ClassNameWrappedTask<T> extends SimpleTask<T> {

	private static final long serialVersionUID = 4898211757076340898L;

	private String className;

	public ClassNameWrappedTask() {
	}

	public ClassNameWrappedTask(TaskId taskId, T data, String className) {
		this.setTaskId(taskId);
		this.setData(data);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

}
