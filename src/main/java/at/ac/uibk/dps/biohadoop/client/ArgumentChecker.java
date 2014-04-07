package at.ac.uibk.dps.biohadoop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentChecker {

	private static Logger logger = LoggerFactory
			.getLogger(ArgumentChecker.class);

	public static boolean checkArgs(String[] args) {
		logger.debug("Checking arguments, length: " + args.length);
		for (String s : args) {
			logger.debug(s);
		}
		
		if (args.length != 2) {
			logger.error("Wrong number of arguments");
			printArgumentUsage();
			return false;
		}
		try {
			Class.forName(args[0]);
//			TODO Check if class implements interface
//			TODO Check if worker impl is available 
		} catch(ClassNotFoundException e) {
			logger.error("Class \"" + args[0] + "\" could not be found");
			printArgumentUsage();
			return false;
		}
		try {
			Integer.parseInt(args[1]);
		} catch(NumberFormatException e) {
			logger.error("Number of containers is not a valid integer: " + args[1]);
			printArgumentUsage();
			return false;
		}
		logger.debug("All arguments valid: " + args);
		return true;
	}

	public static void printArgumentUsage() {
		String usage = "Usage: biohadoop ALGORITHM NUMBER_OF_CONTAINERS"
				+ "\tALGORITHM: full class name of algorithm"
				+ "\tNUMBER_OF_CONTAINERS: number of containers";
		logger.info(usage);
	}
}
