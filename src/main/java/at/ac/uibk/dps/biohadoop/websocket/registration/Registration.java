package at.ac.uibk.dps.biohadoop.websocket.registration;

import at.ac.uibk.dps.biohadoop.job.IdGenerator;

public class Registration {

	private long id = IdGenerator.getId();
	private Object data;

	public Registration() {}
	
	public Registration(Object data) {
		super();
		this.data = data;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
