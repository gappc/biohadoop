package at.ac.uibk.dps.biohadoop.communication;

public interface RemoteExecutable<R, T, S> {

	public S compute(T data, R initialData) throws ComputeException;
	
}
