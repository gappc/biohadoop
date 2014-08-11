package at.ac.uibk.dps.biohadoop.communication.master;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.rest.DefaultRestEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedRest {
	
	String queueName();
	
	Class<? extends MasterLifecycle> master() default DefaultRestEndpoint.class;
	
}
