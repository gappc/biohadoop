package at.ac.uibk.dps.biohadoop.communication.master.socket2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SocketMaster {

	String queueName();
	
}
