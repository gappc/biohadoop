package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;

@Retention(RetentionPolicy.RUNTIME)
public @interface KryoMaster {

	String queueName();
	
	Class<? extends MasterLifecycle> lifecycle() default KryoMasterServer.class;
	
}
