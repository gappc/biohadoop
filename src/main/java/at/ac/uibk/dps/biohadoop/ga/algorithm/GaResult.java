package at.ac.uibk.dps.biohadoop.ga.algorithm;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class GaResult implements Task, Slotted {

	private long id;
	private int slot;
	private double result;

	public GaResult() {
	}

	public GaResult(int slot, double result) {
		super();
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

	@Override
	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public double getResult() {
		return result;
	}

	public void setResult(double result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Slot: " + slot + " | solution: " + result;
	}
}
