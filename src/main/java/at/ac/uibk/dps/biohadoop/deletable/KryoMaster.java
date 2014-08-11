package at.ac.uibk.dps.biohadoop.deletable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface KryoMaster {

	String queueName();
	
	Class<? extends MasterEndpoint> lifecycle() default KryoMasterServer.class;
	
}
