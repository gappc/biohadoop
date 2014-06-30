package at.ac.uibk.dps.biohadoop.deletable;

import java.io.Serializable;

public class Message<T> implements Serializable {

	private static final long serialVersionUID = -6406631824326170469L;
	
	private MessageType type;
	private Task<T> payload;

	public Message() {
	}

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
	
	@Override
	public String toString() {
		return type.toString();
	}
}
