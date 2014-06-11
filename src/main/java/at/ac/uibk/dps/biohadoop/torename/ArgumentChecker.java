package at.ac.uibk.dps.biohadoop.torename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArgumentChecker {

	private static final Logger LOG = LoggerFactory.getLogger(ArgumentChecker.class);
	
	public static boolean isArgumentCountValid(String[] args, int count) {
		if (args.length != 1) {
			LOG.error("Wrong number of arguments, got {}, expected {}",
					args.length, count);
			return false;
		}
		return true;
	}
}
