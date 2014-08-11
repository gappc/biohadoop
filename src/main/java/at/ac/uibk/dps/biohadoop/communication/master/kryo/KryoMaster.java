package at.ac.uibk.dps.biohadoop.communication.master.kryo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

@Retention(RetentionPolicy.RUNTIME)
public @interface KryoMaster {

	String queueName();
	
	Class<? extends MasterEndpoint> lifecycle() default KryoMasterServer.class;
	
}
