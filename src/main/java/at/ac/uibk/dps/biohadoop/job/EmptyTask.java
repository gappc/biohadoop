package at.ac.uibk.dps.biohadoop.job;

public class EmptyTask implements Task {

	private long id;
	
	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

}
