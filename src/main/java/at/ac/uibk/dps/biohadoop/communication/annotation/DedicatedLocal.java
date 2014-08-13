package at.ac.uibk.dps.biohadoop.communication.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.local.DefaultLocalEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedLocal {

	String queueName();
	
	Class<? extends MasterEndpoint> master() default DefaultLocalEndpoint.class;
	
}
