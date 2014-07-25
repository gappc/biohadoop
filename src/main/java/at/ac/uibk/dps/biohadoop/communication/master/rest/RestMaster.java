package at.ac.uibk.dps.biohadoop.communication.master.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RestMaster {

	String path();

	String queueName();
	
	Class<?> receive();
	
}