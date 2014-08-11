package at.ac.uibk.dps.biohadoop.communication.worker;

@Deprecated
public interface Worker<T, S> {

	// TODO: throw exception
	public void readRegistrationObject(Object data);

	// TODO: throw exception
	public S compute(T data);

}
