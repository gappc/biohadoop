package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import javax.naming.Context;

import org.jboss.weld.environment.se.Weld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.server.UndertowServer;

public class WeldLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(UndertowServer.class);
	
	private static final Weld WELD = new Weld();
	
	public static void startWeld() {
		LOG.info("Starting Weld");
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		WELD.initialize();
	}
	
	public static void stopWeld() {
		LOG.info("Stopping Weld");
		WELD.shutdown();
	}
}
