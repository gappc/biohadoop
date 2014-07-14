package at.ac.uibk.dps.biohadoop.webserver;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.handler.distribution.DistributionResource;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.webserver.deployment.ResteasyHandler;
import at.ac.uibk.dps.biohadoop.webserver.deployment.WebSocketHandler;

public class UndertowServer {

	private static final Logger LOG = LoggerFactory
			.getLogger(UndertowServer.class);

	private Undertow undertow;
	private WebSocketHandler webSocket;

	public void start() throws StartServerException {
		LOG.info("Starting Undertow");

		final String host = HostInfo.getHostname();
		final int port = HostInfo.getPort(30000);
		Environment.set(Environment.HTTP_HOST, host);
		Environment.set(Environment.HTTP_PORT, Integer.toString(port));

		try {
			undertow = Undertow.builder().addHttpListener(port, host)
					.setHandler(getPathHandler()).build();
			undertow.start();
			LOG.info("Undertow started at " + host + ":" + port);
		} catch (IllegalArgumentException | IOException | ServletException e) {
			LOG.error("Could not start Undertow", e);
			throw new StartServerException(e);
		}
	}

	public void stop() throws StopServerException {
		LOG.info("Stopping Undertow");
		undertow.stop();
	}

	private PathHandler getPathHandler() throws IOException, ServletException {

		String resteasyContextPath = "/rs";
		ResteasyHandler resteasyHandler = new ResteasyHandler();
		List<Class<?>> restfulClasses = DeployingClasses.getRestfulClasses();
		restfulClasses.add(DistributionResource.class);
		
		List<Class<?>> providerClasses = new ArrayList<Class<?>>();
		
		HttpHandler httpHandler = resteasyHandler.getHandler(
				resteasyContextPath, restfulClasses, providerClasses);

		List<Class<?>> webSocketClasses = DeployingClasses
				.getWebSocketClasses();
		String webSocketContextPath = "/websocket";
		webSocket = new WebSocketHandler();
		HttpHandler webSocketHandler = webSocket.getHandler(
				webSocketContextPath, webSocketClasses);

		return new PathHandler().addPrefixPath(resteasyContextPath, httpHandler)
				.addPrefixPath(webSocketContextPath, webSocketHandler);
	}
}
