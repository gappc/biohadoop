package at.ac.uibk.dps.biohadoop.queue;

public class Monitor {

	private boolean wasSignalled = false;

	public boolean isWasSignalled() {
		return wasSignalled;
	}

	public void setWasSignalled(boolean wasSignalled) {
		this.wasSignalled = wasSignalled;
	}
}
