package at.ac.uibk.dps.biohadoop.server;

import static io.undertow.Handlers.path;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.rs.GaResource;
import at.ac.uibk.dps.biohadoop.rs.SimpleRest;
import at.ac.uibk.dps.biohadoop.server.deployment.ResteasyHandler;
import at.ac.uibk.dps.biohadoop.server.deployment.WebSocketHandler;
import at.ac.uibk.dps.biohadoop.torename.Hostname;

public class UndertowServer {

	private static Logger logger = LoggerFactory.getLogger(UndertowServer.class);

	private Undertow server;

	public void startServer() throws IllegalArgumentException, IOException,
			ServletException {
		logger.info("Starting Undertow");

		server = Undertow.builder()
				.addHttpListener(30000, Hostname.getHostname())
				.setHandler(getPathHandler()).build();
		server.start();
		logger.info("Undertow started at " + Hostname.getHostname());
	}

	private PathHandler getPathHandler() throws IllegalArgumentException,
			IOException, ServletException {
		String resteasyContextPath = "/rs";
		ResteasyHandler resteasy = new ResteasyHandler();
		HttpHandler resteasyHandler = resteasy.getHandler(resteasyContextPath,
				Arrays.asList(SimpleRest.class, GaResource.class), null);

		String webSocketContextPath = "/websocket";
		WebSocketHandler webSocket = new WebSocketHandler();
		HttpHandler webSocketHandler = webSocket
				.getHandler(webSocketContextPath);

		return path().addPrefixPath("/rs", resteasyHandler).addPrefixPath(
				"/websocket", webSocketHandler);
	}

	public void stopServer() {
		if (server != null) {
			logger.info("Stopping Undertow");
			server.stop();
		}
	}
}
