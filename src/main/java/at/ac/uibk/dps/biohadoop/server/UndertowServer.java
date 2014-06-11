package at.ac.uibk.dps.biohadoop.server;

import static io.undertow.Handlers.path;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.distributionmanager.DistributionResource;
import at.ac.uibk.dps.biohadoop.ga.master.rest.GaRestResource;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.server.deployment.ResteasyHandler;
import at.ac.uibk.dps.biohadoop.server.deployment.WebSocketHandler;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

public class UndertowServer implements ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(UndertowServer.class);

	private Undertow server;

	private WebSocketHandler webSocket;
	
	public UndertowServer() {
		try {
			ApplicationManager.getInstance().registerShutdownHandler(this);
			startServer();
		} catch (IllegalArgumentException | IOException | ServletException e) {
			LOG.error("Error while running Undertow", e);
			stopServer();
		}
	}

	public void startServer() throws IllegalArgumentException, IOException,
			ServletException {
		LOG.info("Starting Undertow");

		final String httpHost = HostInfo.getHostname();
		final int httpPort = HostInfo.getPort(30000);
		Environment.set(Environment.HTTP_HOST, httpHost);
		Environment.set(Environment.HTTP_PORT, Integer.toString(httpPort));
		
		server = Undertow.builder()
				.addHttpListener(httpPort, httpHost)
				.setHandler(getPathHandler()).build();
		server.start();
		LOG.info("Undertow started at " + HostInfo.getHostname());
	}

	private PathHandler getPathHandler() throws IllegalArgumentException,
			IOException, ServletException {
		String resteasyContextPath = "/rs";
		ResteasyHandler resteasy = new ResteasyHandler();

		List<Class<?>> resources = new ArrayList<Class<?>>();
		resources.add(GaRestResource.class);
		resources.add(DistributionResource.class);

		HttpHandler resteasyHandler = resteasy.getHandler(resteasyContextPath,
				resources, null);

		String webSocketContextPath = "/websocket";
		webSocket = new WebSocketHandler();
		HttpHandler webSocketHandler = webSocket
				.getHandler(webSocketContextPath);

		return path().addPrefixPath("/rs", resteasyHandler).addPrefixPath(
				"/websocket", webSocketHandler);
	}

	public void stopServer() {
		LOG.info("Stopping Undertow");
		if (server != null) {
//			TODO really needed?
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				LOG.error("Error during server shutdown sleep", e);
			}
			webSocket.stop();
			server.stop();
		}
	}

	@Override
	public void shutdown() {
		stopServer();
	}
}
