package at.ac.uibk.dps.biohadoop.communication.master;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.kryo.DefaultKryoServer;

@Retention(RetentionPolicy.RUNTIME)
public @interface DedicatedKryo {

	String queueName();
	
	Class<? extends MasterEndpoint> master() default DefaultKryoServer.class;
	
}
