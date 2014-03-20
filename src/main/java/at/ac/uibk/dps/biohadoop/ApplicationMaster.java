package at.ac.uibk.dps.biohadoop;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.standalone.Application;

public class ApplicationMaster {
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationMaster.class);
	
	public static void main(String[] args) throws Exception {
		logger.info("############ Starting application master ##########");
		
		System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		
		Weld weld = new Weld();
		WeldContainer container = weld.initialize();
		Application application = container.instance()
				.select(Application.class).get();
		application.run(args);
		weld.shutdown();
		
		logger.info("############ Stopping application master ##########");
	}
	

}
