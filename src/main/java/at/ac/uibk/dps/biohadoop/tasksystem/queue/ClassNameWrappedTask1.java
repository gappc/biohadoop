package at.ac.uibk.dps.biohadoop.tasksystem.queue;


public class ClassNameWrappedTask1<T> {// extends SimpleTask<T> {

	private static final long serialVersionUID = 4898211757076340898L;

	private String className;
	private TaskTypeId uniqueIdentifier;

	public ClassNameWrappedTask1() {
	}

//	public ClassNameWrappedTask1(TaskId taskId, T data, String className,
//			TaskTypeId uniqueIdentifier) {
//		this.setTaskId(taskId);
//		this.setData(data);
//		this.className = className;
//		this.uniqueIdentifier = uniqueIdentifier;
//	}

	public String getClassName() {
		return className;
	}

	public TaskTypeId getUniqueIdentifier() {
		return uniqueIdentifier;
	}

}
