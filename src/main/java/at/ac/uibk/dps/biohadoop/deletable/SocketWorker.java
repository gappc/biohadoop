package at.ac.uibk.dps.biohadoop.deletable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface SocketWorker {

	Class<? extends Master> master();

}
