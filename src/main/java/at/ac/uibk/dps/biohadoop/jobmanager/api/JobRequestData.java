package at.ac.uibk.dps.biohadoop.jobmanager.api;

public class JobRequestData<T> {

	private T data;
	private int slot;
	
	public JobRequestData(T data, int slot) {
		this.data = data;
		this.slot = slot;
	}

	public T getData() {
		return data;
	}

	public int getSlot() {
		return slot;
	}
}
