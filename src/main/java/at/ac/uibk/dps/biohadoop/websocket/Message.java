package at.ac.uibk.dps.biohadoop.websocket;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Message implements Externalizable {

	private static final long serialVersionUID = 5855511080071800033L;

	private MessageType type;
	private Object data;

	public Message() {
	}

	public Message(MessageType type, Object data) {
		super();
		this.type = type;
		this.data = data;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(type.ordinal());
		out.writeObject(data);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		type = MessageType.values()[in.readInt()];
		data = in.readObject();
	}
}
