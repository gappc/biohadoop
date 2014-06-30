package at.ac.uibk.dps.biohadoop.deletable;

public class TaskResponse<T> {

	private Task<T> data;
	private int slot;

	public TaskResponse(Task<T> data, int slot) {
		this.data = data;
		this.slot = slot;
	}

	public Task<T> getData() {
		return data;
	}

	public int getSlot() {
		return slot;
	}
}
