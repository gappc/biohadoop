package at.ac.uibk.dps.biohadoop.server;

import static io.undertow.Handlers.path;
import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.OptionMap;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

import at.ac.uibk.dps.biohadoop.rs.GaResource;
import at.ac.uibk.dps.biohadoop.rs.SimpleRest;
import at.ac.uibk.dps.biohadoop.torename.CDIListener;
import at.ac.uibk.dps.biohadoop.torename.Hostname;
import at.ac.uibk.dps.biohadoop.websocket.GaWebSocketResource;
import at.ac.uibk.dps.biohadoop.websocket.WebSocketRegistration;

public class ServerLoader {

	private static Logger logger = LoggerFactory.getLogger(ServerLoader.class);

	private Undertow server;

	public void startServer() {
		// ResteasyDeployment deployment = new ResteasyDeployment();
		// deployment.getActualResourceClasses().addAll(
		// Arrays.asList(SimpleRest.class, GaResource.class));
		// deployment.getActualProviderClasses().addAll(new ArrayList<Class>());
		// deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
		//
		// ListenerInfo listener = Servlets.listener(CDIListener.class);
		//
		// ServletInfo resteasyServlet = servlet("ResteasyServlet",
		// HttpServlet30Dispatcher.class).setAsyncSupported(true)
		// .setLoadOnStartup(1).addMapping("/");
		//
		// logger.info(new Date() + " 1");
		//
		// DeploymentInfo di = new DeploymentInfo()
		// .addListener(listener)
		// .setContextPath("/")
		// .addServletContextAttribute(ResteasyDeployment.class.getName(),
		// deployment).addServlet(resteasyServlet)
		// .setDeploymentName("ResteasyUndertow")
		// .setClassLoader(Thread.currentThread().getContextClassLoader());
		//
		// logger.info(new Date() + " 2");
		//
		// DeploymentManager deploymentManager =
		// Servlets.defaultContainer().addDeployment(di);
		// deploymentManager.deploy();

		logger.info(new Date() + " 3");

		try {
			logger.info(new Date() + " 4");

			server = Undertow.builder()
					.addHttpListener(30000, Hostname.getHostname())
					// .setHandler(deploymentManager.start())
					.setHandler(getPathHandler()).build();
			logger.info(new Date() + " 5");
			server.start();
			logger.info("Undertow started at " + Hostname.getHostname());
			// } catch (ServletException e) {
			// logger.error("Servlet exception while undertow start", e);
		} catch (IOException e) {
			logger.error("IO exception while undertow start", e);
		}
	}

	private HttpHandler getRestHandler() {
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.getActualResourceClasses().addAll(
				Arrays.asList(SimpleRest.class, GaResource.class));
		deployment.getActualProviderClasses().addAll(new ArrayList<Class>());
		deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());

		ListenerInfo listener = Servlets.listener(CDIListener.class);

		ServletInfo resteasyServlet = servlet("ResteasyServlet",
				HttpServlet30Dispatcher.class).setAsyncSupported(true)
				.setLoadOnStartup(1).addMapping("/");

		logger.info(new Date() + " 1");

		DeploymentInfo di = new DeploymentInfo()
				.addListener(listener)
				.setContextPath("/rs")
				.addServletContextAttribute(ResteasyDeployment.class.getName(),
						deployment).addServlet(resteasyServlet)
				.setDeploymentName("ResteasyUndertow")
				.setClassLoader(Thread.currentThread().getContextClassLoader());

		logger.info(new Date() + " 2");

		DeploymentManager deploymentManager = Servlets.defaultContainer()
				.addDeployment(di);
		deploymentManager.deploy();

		try {
			return deploymentManager.start();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private HttpHandler getWebSocketHandler() throws IllegalArgumentException,
			IOException {
		DeploymentInfo di = getWebSocketDeploymentInfo();

		DeploymentManager deploymentManager = Servlets.defaultContainer()
				.addDeployment(di);
		deploymentManager.deploy();

		try {
			return deploymentManager.start();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private DeploymentInfo getWebSocketDeploymentInfo() throws IllegalArgumentException, IOException {
		final Xnio xnio = Xnio.getInstance("nio",
				Undertow.class.getClassLoader());
		final XnioWorker xnioWorker = xnio.createWorker(OptionMap.builder()
				.getMap());
		final WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo()
				.addEndpoint(GaWebSocketResource.class).addEndpoint(WebSocketRegistration.class).setWorker(
						xnioWorker);

		DeploymentInfo di = new DeploymentInfo()
				.setClassLoader(Thread.currentThread().getContextClassLoader())
				.setContextPath("/websocket")
				.setDeploymentName("embedded-websockets")
				.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets);
		return di;
	}

	private PathHandler getPathHandler() throws IllegalArgumentException,
			IOException {
		return path()
				.addPrefixPath("/websocket", getWebSocketHandler())
				.addPrefixPath("/rs", getRestHandler());
//				.addPrefixPath("/websocket",
//						websocket(new WebSocketConnectionCallback() {
//
//							@Override
//							public void onConnect(
//									WebSocketHttpExchange exchange,
//									WebSocketChannel channel) {
//								channel.getReceiveSetter().set(
//										new AbstractReceiveListener() {
//											@Override
//											protected void onFullTextMessage(
//													WebSocketChannel channel,
//													BufferedTextMessage message) {
//												String data = message.getData();
//												logger.info("Received data: "
//														+ data);
//												WebSockets.sendText(data,
//														channel, null);
//												logger.info("data returned");
//												WebSockets.sendClose(new CloseMessage(CloseMessage.WRONG_CODE, "und tschüss...").toByteBuffer(), channel, new WebSocketCallback<Void>() {
//													
//													@Override
//													public void onError(WebSocketChannel channel, Void context,
//															Throwable throwable) {
//														System.out.println("ERROR");
//													}
//													
//													@Override
//													public void complete(WebSocketChannel channel, Void context) {
//														System.out.println("COMPLETE");														
//													}
//												});
//											}
//										});
//								channel.resumeReceives();
//							}
//						}))
	}

	public void stopServer() {
		if (server != null) {
			server.stop();
		}
	}
}
