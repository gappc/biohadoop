package at.ac.uibk.dps.biohadoop.communication.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.socket.DefaultSocketConnection;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedSocket {

	String queueName();
	
	Class<? extends MasterEndpoint> master() default DefaultSocketConnection.class;
	
}