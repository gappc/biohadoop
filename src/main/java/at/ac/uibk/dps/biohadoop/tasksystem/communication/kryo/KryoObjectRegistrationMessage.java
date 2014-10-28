package at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo;

public class KryoObjectRegistrationMessage {

	private final String className;

	public KryoObjectRegistrationMessage(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
	
}
