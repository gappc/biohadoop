package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.deletable.Worker;

@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMaster {

	String queueName();
	
	Class<? extends Worker<?, ?>> localWorker();
	
	Class<? extends MasterEndpoint> lifecycle() default LocalMasterEndpoint.class;
	
}
