package at.ac.uibk.dps.biohadoop.communication.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.DefaultWebSocketMaster;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedWebSocket {

	String queueName();
	
	Class<? extends MasterEndpoint> master() default DefaultWebSocketMaster.class;
	
}
