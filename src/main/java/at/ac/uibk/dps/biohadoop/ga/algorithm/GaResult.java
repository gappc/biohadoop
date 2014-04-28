package at.ac.uibk.dps.biohadoop.ga.algorithm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class GaResult implements Task, Slotted, Externalizable {

	private static final long serialVersionUID = -5779890460327400560L;
	
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(slot);
		out.writeDouble(result);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = in.readLong();
		slot = in.readInt();
		result = in.readDouble();
	}
}
