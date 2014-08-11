package at.ac.uibk.dps.biohadoop.communication;

import java.io.Serializable;

import at.ac.uibk.dps.biohadoop.queue.Task;

public class Message<T> implements Serializable {

	private static final long serialVersionUID = -6406631824326170469L;

	private MessageType type;
//	@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@taskClass")
	private Task<T> task;

	public Message() {
	}

	public Message(MessageType type, Task<T> task) {
		this.type = type;
		this.task = task;
	}

	public MessageType getType() {
		return type;
	}

	public Task<T> getTask() {
		return task;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
