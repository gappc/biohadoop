package at.ac.uibk.dps.biohadoop.standalone;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class JettyServer {
	
	private Server server;
	
	public void startServer() {
		new Thread() {
			@Override
			public void run() {
				server = new Server(30000);
				Context root = new Context(server, "/", Context.SESSIONS);
				root.addServlet(
						new ServletHolder(new ServletContainer(
								new PackagesResourceConfig(
										"at.ac.uibk.dps.biohadoop.rs"))), "/");
				try {
					server.start();
					server.join();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public void stopServer() throws Exception {
		server.stop();
	}
}