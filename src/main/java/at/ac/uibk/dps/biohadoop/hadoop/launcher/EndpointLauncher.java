package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterLifecycle;
import at.ac.uibk.dps.biohadoop.communication.master.Master;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoMaster;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.KryoSuperServer;
import at.ac.uibk.dps.biohadoop.communication.master.local.LocalMaster;
import at.ac.uibk.dps.biohadoop.communication.master.local.LocalSuperEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.rest.ResourcePath;
import at.ac.uibk.dps.biohadoop.communication.master.rest.RestMaster;
import at.ac.uibk.dps.biohadoop.communication.master.rest.RestSuperMaster;
import at.ac.uibk.dps.biohadoop.communication.master.socket.SocketMaster;
import at.ac.uibk.dps.biohadoop.communication.master.socket.SocketSuperServer;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketMaster;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.WebSocketSuperMaster;
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
	public void startMasterEndpoints() throws EndpointException {
		try {
			LOG.info("Configuring master endpoints");
			// TODO make it possible to extend Biohadoop with new endpoints by
			// simply implementing MasterLifecycle
			// for (Class<? extends MasterLifecycle> endpointClass :
			// communicationConfiguration
			// .getMasterEndpoints()) {
			// LOG.debug("Configuring master endpoint {}", endpointClass);
			// MasterLifecycle masterConnection = endpointClass.newInstance();
			// masterConnection.configure();
			// masterConnections.add(masterConnection);
			// }

			for (Class<? extends Master> endpointClass : communicationConfiguration
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
					SocketSuperServer socketSuperServer = new SocketSuperServer(
							endpointClass);
					socketSuperServer.configure();
					masterConnections.add(socketSuperServer);
					// socketSuperServer.start();
				}
				// ///////////////////////////////////////

				// ///////////KRYO///////////////////////////
				Annotation kryoMasterAnnotation = endpointClass
						.getAnnotation(KryoMaster.class);
				if (kryoMasterAnnotation != null) {
					KryoSuperServer kryoSuperServer = new KryoSuperServer(
							endpointClass);
					kryoSuperServer.configure();
					masterConnections.add(kryoSuperServer);
					// kryoSuperServer.start();
				}
				// ///////////////////////////////////////

				// ///////////LOCAL///////////////////////////
				Annotation localMasterAnnotation = endpointClass
						.getAnnotation(LocalMaster.class);
				if (localMasterAnnotation != null) {
					LocalSuperEndpoint localSuperEndpoint = new LocalSuperEndpoint(
							((LocalMaster) localMasterAnnotation).localWorker());
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
