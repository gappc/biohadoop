package at.ac.uibk.dps.biohadoop.worker;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import at.ac.uibk.dps.biohadoop.rs.SimpleRest;

@ApplicationPath("/")
public class MyApp extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		HashSet<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(SimpleRest.class);
		return classes;
	}

}
