package at.ac.uibk.dps.biohadoop.communication;

public interface RemoteExecutable<R, T, S> {

	public R getInitalData();
	
	public S compute(T data, R initialData);
	
}
