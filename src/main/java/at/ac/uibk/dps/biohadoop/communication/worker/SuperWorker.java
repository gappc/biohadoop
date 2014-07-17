package at.ac.uibk.dps.biohadoop.communication.worker;


public interface SuperWorker<T, S> {

	public void readRegistrationObject(Object data);

	public S compute(T data);
	
}
