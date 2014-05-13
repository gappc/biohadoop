package at.ac.uibk.dps.biohadoop.performance.test;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class JobResponse implements Externalizable {

	private static final long serialVersionUID = 8028540377926146177L;
	public State state;
	public Long job;

	public JobResponse() {
	}

	public JobResponse(State state, Long job) {
		super();
		this.state = state;
		this.job = job;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Long getJob() {
		return job;
	}

	public void setJob(Long job) {
		this.job = job;
	}

	public enum State {
		DIE, STANDBY, RUNJOB
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(state.ordinal());
		out.writeLong(job);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		state = State.values()[in.readInt()];
		job = in.readLong();
	}
}
