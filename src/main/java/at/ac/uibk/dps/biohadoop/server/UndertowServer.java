package at.ac.uibk.dps.biohadoop.server;

import static io.undertow.Handlers.path;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.applicationmanager.ApplicationManager;
import at.ac.uibk.dps.biohadoop.applicationmanager.ShutdownHandler;
import at.ac.uibk.dps.biohadoop.distributionmanager.DistributionResource;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.server.deployment.DeployingClasses;
import at.ac.uibk.dps.biohadoop.server.deployment.ResteasyHandler;
import at.ac.uibk.dps.biohadoop.server.deployment.WebSocketHandler;
import at.ac.uibk.dps.biohadoop.torename.HostInfo;

public class UndertowServer implements ShutdownHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(UndertowServer.class);

	private Undertow server;
	private WebSocketHandler webSocket;

	public UndertowServer() {
		ApplicationManager.getInstance().registerShutdownHandler(this);
	}

	public void startServer() {
		LOG.info("Starting Undertow");

		final String host = HostInfo.getHostname();
		final int port = HostInfo.getPort(30000);
		Environment.set(Environment.HTTP_HOST, host);
		Environment.set(Environment.HTTP_PORT, Integer.toString(port));

		LOG.info("host: " + host + "  port: " + port);

		try {
			server = Undertow.builder().addHttpListener(port, host)
					.setHandler(getPathHandler()).build();
			server.start();
			LOG.info("Undertow started at " + HostInfo.getHostname());
		} catch (IllegalArgumentException | IOException | ServletException e) {
			LOG.error("Could not start Undertow", e);
		}
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

		return path().addPrefixPath(resteasyContextPath, httpHandler)
				.addPrefixPath(webSocketContextPath, webSocketHandler);
	}

	public void stopServer() {
		if (server != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						UndertowShutdown.getLatch().await(5000,
								TimeUnit.MILLISECONDS);
						// Is needed because of problems with WebSockets
						Thread.sleep(1000);
						LOG.info("Stopping Undertow");
						webSocket.stop();
						server.stop();
					} catch (InterruptedException e) {
						LOG.error("Error during server shutdown sleep", e);
					}
				}
			}).start();
		}
	}

	@Override
	public void shutdown() {
		stopServer();
	}
}
