package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestMaster {

	String path();

	String queueName();
	
	Class<?> receive();
	
	Class<? extends MasterEndpoint> lifecycle() default RestMasterEndpoint.class;
	
}
