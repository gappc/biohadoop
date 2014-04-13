package at.ac.uibk.dps.biohadoop.server.deployment;

import static io.undertow.servlet.Servlets.servlet;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Gapp
 * 
 *         Helper class to build an Undertow handler for Resteasy
 * 
 */
public class ResteasyHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ResteasyHandler.class);

	/**
	 * Construct and get an Undertow handler for Resteasy
	 * 
	 * @param contextPath
	 *            Path where the resources are found
	 * @param resourceClasses
	 *            Classes that implement {@link javax.ws.rs.Path} and that
	 *            should be loaded by Resteasy. No annotation checking is done
	 * @param providerClasses Classes that implement {@link javax.ws.rs.ext.Provider} and that
	 *            should be loaded by Resteasy. No annotation checking is done
	 * @return {@link io.undertow.server.HttpHandler} that can be deployed on a server
	 * @throws ServletException
	 */
	public HttpHandler getHandler(String contextPath,
			List<Class<?>> resourceClasses, List<Class<?>> providerClasses)
			throws ServletException {
		if (contextPath == null || contextPath.length() == 0) {
			contextPath = "/";
		}
		if (resourceClasses == null) {
			resourceClasses = new ArrayList<Class<?>>();
		}
		if (providerClasses == null) {
			providerClasses = new ArrayList<Class<?>>();
		}

		ResteasyDeployment deployment = buildDeployment(resourceClasses,
				providerClasses);

		DeploymentInfo di = buildDeploymentInfo(deployment, contextPath);

		DeploymentManager deploymentManager = Servlets.defaultContainer()
				.addDeployment(di);
		deploymentManager.deploy();

		return deploymentManager.start();
	}

	private ResteasyDeployment buildDeployment(List<Class<?>> resourceClasses,
			List<Class<?>> providerClasses) {
		LOGGER.debug("Building Resteasy Deployment");
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.getActualResourceClasses().addAll(resourceClasses);
		deployment.getActualProviderClasses().addAll(providerClasses);
		return deployment;
	}

	private DeploymentInfo buildDeploymentInfo(ResteasyDeployment deployment,
			String contextPath) {
		LOGGER.debug("Building Resteasy DeploymentInfo");
		ServletInfo resteasyServlet = buildResteasyServlet();

		return new DeploymentInfo()
				.setContextPath(contextPath)
				.addServletContextAttribute(ResteasyDeployment.class.getName(),
						deployment).addServlet(resteasyServlet)
				.setDeploymentName("ResteasyUndertow")
				.setClassLoader(Thread.currentThread().getContextClassLoader());
	}

	private ServletInfo buildResteasyServlet() {
		LOGGER.debug("Configuring Resteasy servlet");
		return servlet("ResteasyServlet", HttpServlet30Dispatcher.class)
				.setAsyncSupported(true).setLoadOnStartup(1).addMapping("/");
	}

}
