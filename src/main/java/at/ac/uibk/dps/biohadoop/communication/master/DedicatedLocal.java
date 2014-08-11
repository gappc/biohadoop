package at.ac.uibk.dps.biohadoop.communication.master;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.local.LocalMasterEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedLocal {

	String queueName();
	
	Class<? extends MasterLifecycle> master() default LocalMasterEndpoint.class;
	
}
