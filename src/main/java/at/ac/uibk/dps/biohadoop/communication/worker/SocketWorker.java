package at.ac.uibk.dps.biohadoop.communication.worker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import at.ac.uibk.dps.biohadoop.communication.master.rest.SuperComputable;

@Retention(RetentionPolicy.RUNTIME)
public @interface SocketWorker {

	Class<? extends SuperComputable> master();

}
