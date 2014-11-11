package at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo;

public class KryoObjectRegistrationMessage {

	private final String className;
	private final int bufferSize;
	private final int maxBufferSize;

	public KryoObjectRegistrationMessage(String className, int bufferSize,
			int maxBufferSize) {
		this.className = className;
		this.bufferSize = bufferSize;
		this.maxBufferSize = maxBufferSize;
	}

	public String getClassName() {
		return className;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}
	
}
