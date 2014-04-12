package at.ac.uibk.dps.biohadoop.hadoop.master;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationMaster {
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationMaster.class);
	
	public static void main(String[] args) throws Exception {
		logger.info("############ Starting application master ##########");
		logger.info("############ Starting application master ARGS: " + args);
		
		Application application = new Application();
		application.run(args);

		logger.info("############ Stopping application master ##########");
	}

}
