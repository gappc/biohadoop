package at.ac.uibk.dps.biohadoop.hadoop;

import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.torename.ArgumentChecker;
import at.ac.uibk.dps.biohadoop.torename.HdfsUtil;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.torename.LaunchBuilder;
import at.ac.uibk.dps.biohadoop.torename.LaunchBuilderException;

public class ApplicationMaster {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ApplicationMaster.class);
	
	private Launcher launcher;

	public static void main(String[] args) {
		LOGGER.info("ApplicationMaster started at " + Hostname.getHostname());
		long start = System.currentTimeMillis();

		ApplicationMaster master = new ApplicationMaster();
		master.run(args);

		long end = System.currentTimeMillis();
		LOGGER.info("ApplicationMaster stopped, time: {}ms", end - start);
	}

	public void run(String[] args) {
		if (!checkArguments(args)) {
			return;
		}
		try {
			launcher.launch(args[0]);
		} catch (LaunchException e) {
			LOGGER.error("Error while launching application", e);
		}
	}

	private boolean checkArguments(String[] args) {
		LOGGER.info("Checking arguments");
		
		if (!ArgumentChecker.isArgumentCountValid(args, 1)) {
			return false;
		}
		LOGGER.info("args[0]= {}", args[0]);
		if (!HdfsUtil.fileExists(new YarnConfiguration(), args[0])) {
			return false;
		}
		try {
			launcher = LaunchBuilder.buildLauncher(new YarnConfiguration(), args[0]);
			if (!launcher.isConfigurationValid(args[0])) {
				LOGGER.error("Launch configuration {} invalid", args[0]);
				return false;
			}
		} catch (LaunchBuilderException e) {
			LOGGER.error("Could not check config file {}", args[0]);
			return false;
		}
		return true;
	}
}
