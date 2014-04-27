package at.ac.uibk.dps.biohadoop.websocket;

import java.io.Serializable;

public class Message implements Serializable {

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
}
