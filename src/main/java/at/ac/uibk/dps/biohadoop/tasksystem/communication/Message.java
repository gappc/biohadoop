package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;

public class Message implements Serializable {

	private static final long serialVersionUID = -6406631824326170469L;
	private static final Logger LOG = LoggerFactory.getLogger(Message.class);

	private int type;
	private Task<?> task;

	public Message() {
	}

	public Message(int type, Task<?> task) {
		this.type = type;
		this.task = task;
	}

	public int getType() {
		return type;
	}

	public Task<?> getTask() {
		return task;
	}

	@Override
	public String toString() {
		if (type < 0 || type > MessageType.values().length) {
			LOG.error("Type {} out of enum bounds", type);
			return null;
		}
		return MessageType.values()[type].toString();
	}
}
