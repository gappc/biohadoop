package at.ac.uibk.dps.biohadoop.communication.master;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketMasterEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedWebSocket {

	String queueName();
	
	Class<? extends MasterEndpoint> master() default WebSocketMasterEndpoint.class;
	
}
