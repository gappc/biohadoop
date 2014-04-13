package at.ac.uibk.dps.biohadoop.server.deployment;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import at.ac.uibk.dps.biohadoop.ga.master.GaWebSocketResource;

/**
 * @author Christian Gapp
 * 
 *         Helper class to build an Undertow handler for Websockets
 * 
 */
public class WebSocketHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WebSocketHandler.class);

	private XnioWorker xnioWorker;
	
	/**
	 * Needs to be performed manually, else Undertow won't shut down
	 */
	public void stop() {
		xnioWorker.shutdown();
	}
	
	/**
	 * Construct and get an Undertow handler for Resteasy
	 * 
	 * @param contextPath
	 *            Path where the websockets are bound
	 * @return {@link io.undertow.server.HttpHandler} that can be deployed on a
	 *         server
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServletException
	 */
	public HttpHandler getHandler(String contextPath)
			throws IllegalArgumentException, IOException, ServletException {
		if (contextPath == null || contextPath.length() == 0) {
			contextPath = "/";
		}

		DeploymentInfo di = buildDeploymentInfo(contextPath);

		DeploymentManager deploymentManager = Servlets.defaultContainer()
				.addDeployment(di);
		deploymentManager.deploy();

		return deploymentManager.start();
	}
	
	private DeploymentInfo buildDeploymentInfo(String contextPath)
			throws IllegalArgumentException, IOException {
		LOGGER.debug("Building WebSocket DeploymentInfo");
		final Xnio xnio = Xnio.getInstance("nio",
				Undertow.class.getClassLoader());
		xnioWorker = xnio.createWorker(OptionMap.builder()
				.getMap());
		final WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo()
				.addEndpoint(GaWebSocketResource.class)
				.setWorker(xnioWorker);

		return new DeploymentInfo()
				.setClassLoader(Thread.currentThread().getContextClassLoader())
				.setContextPath(contextPath)
				.setDeploymentName("embedded-websockets")
				.addServletContextAttribute(
						WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets);
	}
}
