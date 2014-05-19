package at.ac.uibk.dps.biohadoop.nsgaii.algorithm;

import java.io.Serializable;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class NsgaIITask implements Task, Slotted, Serializable {

	private static final long serialVersionUID = -9019952190368130996L;
	
	private long id;
	private int slot;
	private double[] y;
	
	public NsgaIITask() {
	}
	
	public NsgaIITask(int slot, double[] y) {
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
