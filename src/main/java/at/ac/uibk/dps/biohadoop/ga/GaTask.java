package at.ac.uibk.dps.biohadoop.ga;

public class GaTask {

	private int slot;
	private int[] genome;
	
	public GaTask(){}
	
	public GaTask(int slot, int[] genome) {
		super();
		this.slot = slot;
		this.genome = genome;
	}

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
	
}
