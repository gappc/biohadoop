package at.ac.uibk.dps.biohadoop.tasksystem;

public interface RemoteExecutable<R, T, S> {

	public S compute(T data, R initialData) throws ComputeException;
	
}
