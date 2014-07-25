package at.ac.uibk.dps.biohadoop.communication.master.websocket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WebSocketMaster {

	String path();

	String queueName();

}