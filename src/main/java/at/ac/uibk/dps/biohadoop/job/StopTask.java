package at.ac.uibk.dps.biohadoop.job;

import java.io.Serializable;

public class StopTask implements Task, Serializable {

	private static final long serialVersionUID = -4330113855077499683L;
	
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
