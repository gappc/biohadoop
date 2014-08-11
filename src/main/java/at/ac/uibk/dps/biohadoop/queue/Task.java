package at.ac.uibk.dps.biohadoop.queue;


//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@taskClass")
public interface Task<T> {

	public TaskId getTaskId();
	
	public T getData();
	
}
