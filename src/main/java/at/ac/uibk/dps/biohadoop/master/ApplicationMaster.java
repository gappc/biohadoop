package at.ac.uibk.dps.biohadoop.master;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.loader.CleanClassLoader;
import at.ac.uibk.dps.biohadoop.loader.WeldLoader;
import at.ac.uibk.dps.biohadoop.torename.ClassPathBuilder;

public class ApplicationMaster {
	
	private static Logger logger = LoggerFactory.getLogger(ApplicationMaster.class);
	
	public static void main(String[] args) throws Exception {
		logger.info("############ Starting application master ##########");
		logger.info("############ Starting application master ARGS: " + args);
		logger.info("APPLICATION MASTER CLASSPATH: " + System.getProperty("java.class.path"));
		
		WeldLoader.startWeldContainer(args);
		
//		List<URL> libs = null;
//		if (args.length >= 1 && ("local").equals(args[0])) {
//			libs = ClassPathBuilder.getLocalUrls();
//		}
//		else {
//			libs = ClassPathBuilder.getHadoopUrls();
//		}
//		
//		CleanClassLoader loader = new CleanClassLoader();
//		loader.startCleanClassLoader(args, libs);
		
		logger.info("############ Stopping application master ##########");
	}

}
