package at.ac.uibk.dps.biohadoop.communication.master.kryo2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface KryoMaster {

	String queueName();
	
}
