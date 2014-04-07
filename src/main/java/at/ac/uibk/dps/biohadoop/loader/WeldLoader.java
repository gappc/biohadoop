package at.ac.uibk.dps.biohadoop.loader;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.master.Application;

public class WeldLoader {
	
	private static Logger logger = LoggerFactory.getLogger(WeldLoader.class);
	
	public static void startWeldContainer(String[] args) {
		logger.info("############ Starting isolated application master ##########");
		
		System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		
		Weld weld = new Weld();
		WeldContainer container = weld.initialize();
		Application application = container.instance()
				.select(Application.class).get();
		
		logger.info("########### IsolatedApplicationMaster ARGS: " + args);
		
		application.run(args);
		
		weld.shutdown();
		
		logger.info("FINISH");
	}
}
