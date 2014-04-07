package at.ac.uibk.dps.biohadoop.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.loader.WeldLoader;

public class ApplicationMaster {
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationMaster.class);
	
	public static void main(String[] args) throws Exception {
		logger.info("############ Starting application master ##########");
		logger.info("############ Starting application master ARGS: " + args);
		logger.info("APPLICATION MASTER CLASSPATH: " + System.getProperty("java.class.path"));
		
		WeldLoader.startWeldContainer(args);

		logger.info("############ Stopping application master ##########");
	}

}
