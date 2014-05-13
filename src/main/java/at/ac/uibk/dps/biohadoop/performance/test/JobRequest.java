package at.ac.uibk.dps.biohadoop.performance.test;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

public class JobRequest implements Externalizable {

	private static final long serialVersionUID = -105200337969324728L;
	public State state;
	public Long job;

	public JobRequest() {
	}

	public JobRequest(State state, Long job) {
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
		WHATTODO, JOBDONE, JOBFAILED
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
