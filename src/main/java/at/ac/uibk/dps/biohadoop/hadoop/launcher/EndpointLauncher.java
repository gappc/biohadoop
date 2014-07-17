package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.kryo2.KryoMaster;
import at.ac.uibk.dps.biohadoop.communication.master.kryo2.KryoSuperServer;
import at.ac.uibk.dps.biohadoop.communication.master.local2.LocalMaster;
import at.ac.uibk.dps.biohadoop.communication.master.local2.LocalSuperEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.rest2.ResourcePath;
import at.ac.uibk.dps.biohadoop.communication.master.rest2.RestMaster;
import at.ac.uibk.dps.biohadoop.communication.master.rest2.RestSuperMaster;
import at.ac.uibk.dps.biohadoop.communication.master.rest2.SuperComputable;
import at.ac.uibk.dps.biohadoop.communication.master.socket2.SocketMaster;
import at.ac.uibk.dps.biohadoop.communication.master.socket2.SocketSuperServer;
import at.ac.uibk.dps.biohadoop.communication.master.websocket2.WebSocketMaster;
import at.ac.uibk.dps.biohadoop.communication.master.websocket2.WebSocketSuperMaster;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.webserver.StartServerException;
import at.ac.uibk.dps.biohadoop.webserver.UndertowServer;
import at.ac.uibk.dps.biohadoop.webserver.deployment.DeployingClasses;

public class EndpointLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(EndpointLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	private List<MasterLifecycle> masterConnections = new ArrayList<>();
	private UndertowServer undertowServer;

	public EndpointLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	// TODO: what happens if any endpoint throws exception?
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

			for (Class<? extends SuperComputable> endpointClass : communicationConfiguration
					.getMasters()) {
				LOG.debug("Configuring SUPER master endpoint {}", endpointClass);

				// ///////////REST///////////////////////////
				Annotation restMasterAnnotation = endpointClass
						.getAnnotation(RestMaster.class);
				// TODO prefix "rs-" for ResourcePath
				if (restMasterAnnotation != null) {
					ResourcePath.addRestEntry(
							((RestMaster) restMasterAnnotation).path(),
							endpointClass);
					DeployingClasses.addRestfulClass(RestSuperMaster.class);
				}
				// ///////////WEBSOCKET///////////////////////////
				Annotation wsMasterAnnotation = endpointClass
						.getAnnotation(WebSocketMaster.class);
				if (wsMasterAnnotation != null) {
					// TODO prefix "ws-" for ResourcePath
					ResourcePath.addWebSocketEntry(
							((WebSocketMaster) wsMasterAnnotation).path(),
							endpointClass);
					DeployingClasses
							.addWebSocketClass(WebSocketSuperMaster.class);
				}

				// ///////////SOCKET///////////////////////////
				Annotation socketMasterAnnotation = endpointClass
						.getAnnotation(SocketMaster.class);
				if (socketMasterAnnotation != null) {
					SuperComputable testMaster = endpointClass.newInstance();
					SocketSuperServer socketSuperServer = new SocketSuperServer(
							((SocketMaster) socketMasterAnnotation).queueName(),
							testMaster.getRegistrationObject());
					socketSuperServer.configure();
					masterConnections.add(socketSuperServer);
					// socketSuperServer.start();
				}
				// ///////////////////////////////////////

				// ///////////KRYO///////////////////////////
				Annotation kryoMasterAnnotation = endpointClass
						.getAnnotation(KryoMaster.class);
				if (kryoMasterAnnotation != null) {
					SuperComputable testMaster2 = endpointClass.newInstance();
					KryoSuperServer kryoSuperServer = new KryoSuperServer(
							((KryoMaster) kryoMasterAnnotation).queueName(),
							testMaster2.getRegistrationObject());
					kryoSuperServer.configure();
					masterConnections.add(kryoSuperServer);
					// kryoSuperServer.start();
				}
				// ///////////////////////////////////////

				// ///////////LOCAL///////////////////////////
				Annotation localMasterAnnotation = endpointClass
						.getAnnotation(LocalMaster.class);
				if (localMasterAnnotation != null) {
					SuperComputable testMaster3 = endpointClass.newInstance();
					LocalSuperEndpoint localSuperEndpoint = new LocalSuperEndpoint(
							((LocalMaster) localMasterAnnotation).localWorker(),
							((LocalMaster) localMasterAnnotation).queueName(),
							testMaster3.getRegistrationObject());
					localSuperEndpoint.configure();
					masterConnections.add(localSuperEndpoint);
				}
				// ///////////////////////////////////////
			}

			undertowServer = new UndertowServer();
			undertowServer.start();

			LOG.info("Starting master endpoints");
			for (MasterLifecycle masterConnection : masterConnections) {
				LOG.debug("Starting master endpoint {}", masterConnection
						.getClass().getCanonicalName());
				masterConnection.start();
			}
		} catch (InstantiationException | IllegalAccessException
				| StartServerException e) {
			LOG.error("Could not start endpoints", e);
			throw new EndpointLaunchException(e);
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
