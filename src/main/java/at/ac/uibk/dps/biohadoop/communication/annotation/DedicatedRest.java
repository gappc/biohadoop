package at.ac.uibk.dps.biohadoop.communication.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.rest.DefaultRestEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedRest {
	
	String queueName();
	
	Class<? extends MasterEndpoint> master() default DefaultRestEndpoint.class;
	
}