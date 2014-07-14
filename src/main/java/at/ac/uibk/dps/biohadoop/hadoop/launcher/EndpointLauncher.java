package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.webserver.StartServerException;
import at.ac.uibk.dps.biohadoop.webserver.UndertowServer;

public class EndpointLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	private List<MasterLifecycle> masterConnections = new ArrayList<>();
	private UndertowServer undertowServer;

	public EndpointLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	public void startMasterEndpoints() throws EndpointLaunchException {
		try {
			LOG.info("Configuring master endpoints");
			for (Class<? extends MasterLifecycle> endpointClass : communicationConfiguration
					.getMasterEndpoints()) {
				LOG.debug("Configuring master endpoint {}", endpointClass);
				MasterLifecycle masterConnection = endpointClass.newInstance();
				masterConnection.configure();
				masterConnections.add(masterConnection);
			}

			undertowServer = new UndertowServer();
			undertowServer.start();

			LOG.info("Starting master endpoints");
			for (MasterLifecycle masterConnection : masterConnections) {
				LOG.debug("Starting master endpoint {}", masterConnection.getClass().getCanonicalName());
				masterConnection.start();
			}
		} catch (InstantiationException | IllegalAccessException
				| StartServerException e) {
			LOG.error("Could not start endpoints", e);
			throw new EndpointLaunchException(e);
		}
	}

	public void stopMasterEndpoints() throws Exception {
		LOG.info("Stopping master endpoints");
		ShutdownWaitingService.setFinished();
		for (MasterLifecycle masterConnection : masterConnections) {
			LOG.debug("Stopping master endpoint {}", masterConnection.getClass().getCanonicalName());
			masterConnection.stop();
		}
		ShutdownWaitingService.await();
		undertowServer.stop();
	}
}
