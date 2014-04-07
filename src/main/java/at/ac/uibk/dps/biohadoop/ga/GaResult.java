package at.ac.uibk.dps.biohadoop.ga;

public class GaResult {

	private int slot;
	private double result;
	
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
