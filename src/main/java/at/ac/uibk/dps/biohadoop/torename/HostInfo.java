package at.ac.uibk.dps.biohadoop.torename;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostInfo {
	
	private static final Logger LOG = LoggerFactory.getLogger(HostInfo.class);
	
	private HostInfo() {
	}

	public static String getHostname() {
		Process process;
		try {
			process = Runtime.getRuntime().exec("hostname");
			BufferedReader is = new BufferedReader(new 
		             InputStreamReader(process.getInputStream()));
			return is.readLine();
		} catch (IOException e) {
			LOG.error("Could not get hostname", e);
			return null;
		}
	}
	
	public static int getPort(int preferredPort) {
		return PortFinder.findFreePort(preferredPort);
	}
}
