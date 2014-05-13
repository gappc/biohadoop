package at.ac.uibk.dps.biohadoop.moead.algorithm;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class MoeadTask implements Task, Slotted {

	private long id;
	private int slot;
	private double[] y;
	
	public MoeadTask() {
	}
	
	public MoeadTask(int slot, double[] y) {
		super();
		this.slot = slot;
		this.y = y;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public void setY(double[] y) {
		this.y = y;
	}

	public double[] getY() {
		return y;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}
	
}
