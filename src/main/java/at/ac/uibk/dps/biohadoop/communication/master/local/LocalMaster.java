package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.worker.Worker;

@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMaster {

	String queueName();
	
	Class<? extends Worker<?, ?>> localWorker();
	
	Class<? extends MasterLifecycle> lifecycle() default LocalMasterEndpoint.class;
	
}
