package at.ac.uibk.dps.biohadoop.server;

import static io.undertow.Handlers.path;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import org.jboss.weld.environment.se.Weld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.server.deployment.ResteasyHandler;
import at.ac.uibk.dps.biohadoop.server.deployment.WebSocketHandler;
import at.ac.uibk.dps.biohadoop.service.distribution.DistributionResource;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

public class UndertowServer {

	private static final Logger LOG = LoggerFactory
			.getLogger(UndertowServer.class);

	private Undertow undertow;
	private WebSocketHandler webSocket;
	private Weld weld;

	public void start() throws StartServerException {
		weld = new Weld();
		weld.initialize();
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
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		undertow.stop();
	}

	private PathHandler getPathHandler() throws IllegalArgumentException,
			IOException, ServletException {

		String resteasyContextPath = "/rs";
		ResteasyHandler resteasyHandler = new ResteasyHandler();
		List<Class<?>> restfulClasses = DeployingClasses.getRestfulClasses();
		restfulClasses.add(DistributionResource.class);
		HttpHandler httpHandler = resteasyHandler.getHandler(
				resteasyContextPath, restfulClasses, null);

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
