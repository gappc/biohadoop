package at.ac.uibk.dps.biohadoop.communication.master.local;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.worker.SuperWorker;

@Retention(RetentionPolicy.RUNTIME)
public @interface LocalMaster {

	String queueName();
	
	Class<? extends SuperWorker<?, ?>> localWorker();
	
}
