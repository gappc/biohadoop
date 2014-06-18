package at.ac.uibk.dps.biohadoop.service.job.api;

public class JobResponseData<T> {
	
	private T data;
	private int slot;
	
	public JobResponseData(T data, int slot) {
		this.data = data;
		this.slot = slot;
	}

	public T getData() {
		return data;
	}

	public int getSlot() {
		return slot;
	}
	
	@Override
	public String toString() {
		return data + "|" + slot;
	}
}
