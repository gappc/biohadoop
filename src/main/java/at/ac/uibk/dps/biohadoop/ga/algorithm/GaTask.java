package at.ac.uibk.dps.biohadoop.ga.algorithm;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class GaTask implements Task, Slotted {

	private long id;
	private int slot;
	private int[] genome;

	public GaTask() {
	}

	public GaTask(int slot, int[] genome) {
		super();
		this.slot = slot;
		this.genome = genome;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public int[] getGenome() {
		return genome;
	}

	public void setGenome(int[] genome) {
		this.genome = genome;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id=").append(id).append("|slot=").append(slot);
		return sb.toString();
	}
}
