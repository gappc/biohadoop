package at.ac.uibk.dps.biohadoop.communication.master.websocket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;

@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketMaster {

	String path();

	String queueName();
	
	Class<? extends MasterLifecycle> lifecycle() default WebSocketMasterEndpoint.class;

}
