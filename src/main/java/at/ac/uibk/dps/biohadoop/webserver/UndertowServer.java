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
import at.ac.uibk.dps.biohadoop.islandmodel.IslandModelResource;
import at.ac.uibk.dps.biohadoop.metrics.CORSFilter;
import at.ac.uibk.dps.biohadoop.metrics.MetricsResource;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.webserver.handler.DeployingClasses;
import at.ac.uibk.dps.biohadoop.webserver.handler.ResteasyHandler;
import at.ac.uibk.dps.biohadoop.webserver.handler.WebSocketHandler;

public class UndertowServer {

	private static final Logger LOG = LoggerFactory
			.getLogger(UndertowServer.class);

	private Undertow undertow;
	private WebSocketHandler webSocket;

	public void start() throws StartServerException {
		LOG.info("Starting Undertow");

		final String host = HostInfo.getHostname();
		final int port = HostInfo.getPort(30000);
		Environment.setPrefixed(Environment.DEFAULT_PREFIX,
				Environment.HTTP_HOST, host);
		Environment.setPrefixed(Environment.DEFAULT_PREFIX,
				Environment.HTTP_PORT, Integer.toString(port));

		try {
			undertow = Undertow.builder().addHttpListener(port, host)
					.setHandler(getUndertowHandlers()).build();
			undertow.start();
			LOG.info("Undertow started at " + host + ":" + port);
		} catch (IllegalArgumentException | IOException | ServletException e) {
			LOG.error("Could not start Undertow", e);
			throw new StartServerException(e);
		}
	}

	public void stop() throws StopServerException {
		LOG.info("Stopping Undertow");
		try {
			// Wait a short period of time to allow workers to recognize that we
			// are shutting down. Important e.g. for Rest workers
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			LOG.error("Error while sleeping", e);
		}
		undertow.stop();
	}

	private PathHandler getUndertowHandlers() throws IOException,
			ServletException {
		String resteasyContextPath = "/rs";
		HttpHandler restHandler = getRestHandler(resteasyContextPath);
		String webSocketContextPath = "/websocket";
		HttpHandler webSocketHandler = getWebSocketHandler(webSocketContextPath);
		String metricsContextPath = "/metrics";
		HttpHandler metricsHandler = getMetricsHandler(metricsContextPath);

		return new PathHandler()
				.addPrefixPath(resteasyContextPath, restHandler)
				.addPrefixPath(webSocketContextPath, webSocketHandler)
				.addPrefixPath(metricsContextPath, metricsHandler);
	}

	private HttpHandler getRestHandler(String resteasyContextPath)
			throws ServletException {
		ResteasyHandler resteasyHandler = new ResteasyHandler();
		List<Class<?>> restfulClasses = DeployingClasses.getRestfulClasses();
		restfulClasses.add(IslandModelResource.class);

		List<Class<?>> providerClasses = new ArrayList<Class<?>>();

		return resteasyHandler.getHandler(resteasyContextPath, restfulClasses,
				providerClasses);
	}

	private HttpHandler getWebSocketHandler(String webSocketContextPath)
			throws IOException, ServletException {
		List<Class<?>> webSocketClasses = DeployingClasses
				.getWebSocketClasses();
		webSocket = new WebSocketHandler();
		return webSocket.getHandler(webSocketContextPath, webSocketClasses);
	}

	private HttpHandler getMetricsHandler(String metricsContextPath)
			throws ServletException {
		ResteasyHandler metricsHandler = new ResteasyHandler();
		List<Class<?>> restfulClasses = DeployingClasses.getRestfulClasses();
		restfulClasses.add(MetricsResource.class);

		List<Class<?>> providerClasses = new ArrayList<Class<?>>();
		providerClasses.add(CORSFilter.class);
		return metricsHandler.getHandler(metricsContextPath, restfulClasses,
				providerClasses);
	}

}
