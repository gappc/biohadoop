package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.webserver.StartServerException;
import at.ac.uibk.dps.biohadoop.webserver.UndertowServer;

public class MasterLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(MasterLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	private List<MasterLifecycle> masterConnections = new ArrayList<>();
	private UndertowServer undertowServer;

	public MasterLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	// TODO: what happens if any endpoint throws exception?
	public void startMasterEndpoints() throws EndpointException {
		try {
			LOG.info("Configuring master endpoints");

			boolean endpointsFound = false;
			for (Class<? extends Master> endpointClass : communicationConfiguration
					.getMasters()) {
				LOG.debug("Configuring master endpoint {}", endpointClass);

				boolean usableAnnotationFound = false;
				Annotation[] annotations = endpointClass.getAnnotations();
				for (Annotation annotation : annotations) {
					LOG.debug(
							"Found annotation {} on possible master endpoint {}",
							annotation, endpointClass);
					Method[] methods = annotation.annotationType().getMethods();
					for (Method method : methods) {
						LOG.debug("Found method {} for annotation {}", method,
								annotation);
						if ("lifecycle".equals(method.getName())) {
							LOG.info(
									"Found suitable annotation {} with \"lifecycle\" method",
									annotation);

							Class<? extends MasterLifecycle> methodReturnType = null;
							try {
								methodReturnType = (Class<? extends MasterLifecycle>) method
										.invoke(annotation);
								MasterLifecycle masterEndpoint = methodReturnType
										.newInstance();
								masterEndpoint.configure(endpointClass);

								masterConnections.add(masterEndpoint);
								endpointsFound = true;
								usableAnnotationFound = true;
							} catch (InstantiationException
									| IllegalAccessException
									| IllegalArgumentException
									| InvocationTargetException e) {
								LOG.error(
										"Could not instanciate {}, endpoint not running",
										methodReturnType.getCanonicalName(), e);
							}
						}
					}
				}
				if (!usableAnnotationFound) {
					LOG.warn(
							"No usable annotation found for declared endpoint class {}, maybe not correctly annotated or annotation doesn't define a \"lifecycle\" method",
							endpointClass);
				}
			}

			if (!endpointsFound) {
				throw new EndpointException(
						"No usable master endpoints found, stopping");
			}

			undertowServer = new UndertowServer();
			undertowServer.start();

			LOG.info("Starting master endpoints");
			for (MasterLifecycle masterConnection : masterConnections) {
				LOG.debug("Starting master endpoint {}", masterConnection
						.getClass().getCanonicalName());
				masterConnection.start();
			}
		} catch (EndpointConfigureException e) {
			throw new EndpointException(e);
		} catch (EndpointLaunchException e) {
			throw new EndpointException(e);
		} catch (StartServerException e) {
			throw new EndpointException(e);
		}
	}

	// TODO: what happens if any endpoint throws exception?
	public void stopMasterEndpoints() throws Exception {
		LOG.info("Stopping master endpoints");
		ShutdownWaitingService.setFinished();
		for (MasterLifecycle masterConnection : masterConnections) {
			LOG.debug("Stopping master endpoint {}", masterConnection
					.getClass().getCanonicalName());
			masterConnection.stop();
		}
		ShutdownWaitingService.await();
		undertowServer.stop();
	}
}
