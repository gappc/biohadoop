package at.ac.uibk.dps.biohadoop.standalone;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.mortbay.jetty.Server;

public class TestServer {

	static Server server;

	public static void main(String[] args) throws Exception {
		System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
		
		Weld weld = new Weld();
		WeldContainer container = weld.initialize();
		Application application = container.instance()
				.select(Application.class).get();
		application.run(args);
		weld.shutdown();
	}
}
