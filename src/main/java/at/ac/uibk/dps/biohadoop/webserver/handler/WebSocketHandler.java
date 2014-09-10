package at.ac.uibk.dps.biohadoop.webserver.handler;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.BufferAllocator;
import org.xnio.ByteBufferSlicePool;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Pool;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

/**
 * @author Christian Gapp
 * 
 *         Helper class to build an Undertow handler for Websockets
 * 
 */
public class WebSocketHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(WebSocketHandler.class);

	/**
	 * Construct and get an Undertow handler for WebSocket
	 * 
	 * @param contextPath
	 *            Path where the websockets are bound
	 * @param webSocketClasses
	 *            List of classes that implement WebSockets
	 * @return {@link io.undertow.server.HttpHandler} that can be deployed on a
	 *         server
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws ServletException
	 */
	public HttpHandler getHandler(String contextPath,
			List<Class<?>> webSocketClasses) throws IOException,
			ServletException {
		if (contextPath == null || contextPath.length() == 0) {
			contextPath = "/";
		}

		DeploymentInfo di = buildDeploymentInfo(contextPath, webSocketClasses);

		DeploymentManager deploymentManager = Servlets.defaultContainer()
				.addDeployment(di);
		deploymentManager.deploy();

		return deploymentManager.start();
	}

	private DeploymentInfo buildDeploymentInfo(String contextPath,
			List<Class<?>> webSocketClasses) throws IOException {
		LOG.debug("Building WebSocket DeploymentInfo");
		final Xnio xnio = Xnio.getInstance("nio",
				Undertow.class.getClassLoader());
		final XnioWorker xnioWorker = xnio.createWorker(OptionMap.builder()
				.set(Options.THREAD_DAEMON, true)
				.set(Options.WORKER_NAME, "WEBSOCKET").getMap());
		final WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo();
		for (Class<?> webSocketClass : webSocketClasses) {
			webSockets.addEndpoint(webSocketClass);
		}
		webSockets.setWorker(xnioWorker);

		if (System.getProperty("local") != null) {
			// We have to manually set the buffer in local mode, else the
			// WebSocket workers don't get assigned any buffer and fail with a
			// NullPointerException
			int bufferSize = 16384;
			int buffersPerRegion = 20;
			Pool<ByteBuffer> buffers = new ByteBufferSlicePool(
					BufferAllocator.DIRECT_BYTE_BUFFER_ALLOCATOR, bufferSize,
					bufferSize * buffersPerRegion);
			webSockets.setBuffers(buffers);
		}

		return new DeploymentInfo()
				.setClassLoader(Thread.currentThread().getContextClassLoader())
				.setContextPath(contextPath)
				.setDeploymentName("embedded-websockets")
				.addServletContextAttribute(
						WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets);
	}
}
