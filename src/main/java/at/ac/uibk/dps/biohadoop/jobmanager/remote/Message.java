package at.ac.uibk.dps.biohadoop.jobmanager.remote;

import at.ac.uibk.dps.biohadoop.jobmanager.Task;
import at.ac.uibk.dps.biohadoop.websocket.MessageType;

public class Message<T> {

	private final MessageType type;
	private final Task<T> payload;

	public Message(MessageType type, Task<T> payload) {
		this.type = type;
		this.payload = payload;
	}

	public MessageType getType() {
		return type;
	}

	public Task<T> getPayload() {
		return payload;
	}
}
