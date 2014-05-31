package at.ac.uibk.dps.biohadoop.jobmanager.remote;

public enum MessageType {
	NONE,
	REGISTRATION_REQUEST,
	REGISTRATION_RESPONSE,
	WORK_INIT_REQUEST,
	WORK_INIT_RESPONSE,
	WORK_REQUEST,
	WORK_RESPONSE,
	SHUTDOWN
}
