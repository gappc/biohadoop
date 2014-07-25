package at.ac.uibk.dps.biohadoop.communication.worker;

public interface SuperWorker<T, S> {

	// TODO: throw exception
	public void readRegistrationObject(Object data);

	// TODO: throw exception
	public S compute(T data);

}
