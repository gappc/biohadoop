package at.ac.uibk.dps.biohadoop.ga.algorithm;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import at.ac.uibk.dps.biohadoop.job.Slotted;
import at.ac.uibk.dps.biohadoop.job.Task;

public class GaTask implements Task, Slotted, Externalizable {

	private static final long serialVersionUID = 3446084594992308113L;
	
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeInt(slot);
		out.writeInt(genome.length);
		for (int g : genome) {
			out.writeInt(g);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = in.readLong();
		slot = in.readInt();
		genome = new int[in.readInt()];
		for (int i = 0; i < genome.length; i++) {
			genome[i] = in.readInt();
		}
	}
}
