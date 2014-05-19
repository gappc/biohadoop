package at.ac.uibk.dps.biohadoop.nsgaii.algorithm;

import java.io.Serializable;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class NsgaIIResult implements Task, Slotted, Serializable {
	
	private static final long serialVersionUID = -2185289745637734698L;
	
	private long id;
	private int slot;
	private double[] result;

	public NsgaIIResult() {
	}

	public NsgaIIResult(long id, int slot, double[] result) {
		super();
		this.id = id;
		this.slot = slot;
		this.result = result;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	public double[] getResult() {
		return result;
	}

	public void setResult(double[] result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Solution: " + result;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}
}
