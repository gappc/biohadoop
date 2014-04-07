package at.ac.uibk.dps.biohadoop.server;

import static io.undertow.servlet.Servlets.servlet;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.rs.GaResource;
import at.ac.uibk.dps.biohadoop.rs.SimpleRest;
import at.ac.uibk.dps.biohadoop.torename.CDIListener;
import at.ac.uibk.dps.biohadoop.torename.Hostname;

public class ServerLoader {
	
	private static Logger logger = LoggerFactory.getLogger(ServerLoader.class);
	
	private Undertow server;

	public void startServer() {
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
				.setContextPath("/")
				.addServletContextAttribute(ResteasyDeployment.class.getName(),
						deployment).addServlet(resteasyServlet)
				.setDeploymentName("ResteasyUndertow")
				.setClassLoader(Thread.currentThread().getContextClassLoader());

		logger.info(new Date() + " 2");
		
		DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(di);
        deploymentManager.deploy();
        
        logger.info(new Date() + " 3");
        
        try {
        	logger.info(new Date() + " 4");
        	
            server = Undertow.builder()
                    .addHttpListener(30000, Hostname.getHostname())
                    .setHandler(deploymentManager.start())
                    .build();
            logger.info(new Date() + " 5");
            server.start();
            logger.info("Undertow started at " + Hostname.getHostname());
        } catch (ServletException e) {
            logger.error("Servlet exception while undertow start", e);
        } catch (IOException e) {
            logger.error("IO exception while undertow start", e);
		}
	}
	
	public void stopServer() {
		if (server != null) {
			server.stop();
		}
	}
}
