package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.MasterException;
import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.launch.DedicatedRemoteExecutableResolver;
import at.ac.uibk.dps.biohadoop.utils.launch.DefaultRemoteExecutableResolver;
import at.ac.uibk.dps.biohadoop.utils.launch.LaunchInformation;
import at.ac.uibk.dps.biohadoop.utils.launch.ResolveDedicatedEndpointException;
import at.ac.uibk.dps.biohadoop.webserver.StartServerException;
import at.ac.uibk.dps.biohadoop.webserver.UndertowServer;

public class MasterLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(MasterLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	private final List<LaunchInformation> launchInformations = new ArrayList<>();
	private UndertowServer undertowServer;

	public MasterLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	// TODO: what happens if any endpoint throws exception?
	public void startMasterEndpoints() throws MasterException {
		try {
			LOG.info("Adding default endpoints");
			launchInformations.addAll(DefaultRemoteExecutableResolver
					.getDefaultEndpoints(communicationConfiguration));

			LOG.info("Adding dedicated endpoints");
			List<Class<? extends RemoteExecutable<?, ?, ?>>> remoteExecutables = communicationConfiguration
					.getMasters();
			launchInformations.addAll(DedicatedRemoteExecutableResolver
					.getDedicatedEndpoints(remoteExecutables));

			if (launchInformations.size() == 0) {
				LOG.warn("No usable endpoints found, maybe default endpoints are overwritten in config file?");
			}

			LOG.info("Configuring endpoints");
			for (LaunchInformation launchInformation : launchInformations) {
				LOG.debug("Configuring endpoint {}", launchInformation);
				MasterEndpoint master = launchInformation.getMaster();
				Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable = launchInformation.getRemoteExecutable();
				master.configure(remoteExecutable);
			}

			undertowServer = new UndertowServer();
			undertowServer.start();

			LOG.info("Starting endpoints");
			for (LaunchInformation launchInformation : launchInformations) {
				LOG.debug("Starting endpoint {}", launchInformation);
				MasterEndpoint master = launchInformation.getMaster();
				master.start();
			}
		} catch (StartServerException e) {
			throw new MasterException(e);
		} catch (ResolveDedicatedEndpointException e) {
			throw new MasterException(e);
		}
	}

	// TODO: what happens if any endpoint throws exception?
	public void stopMasterEndpoints() throws Exception {
		LOG.info("Stopping endpoints");
		
		ShutdownWaitingService.setFinished();

		for (LaunchInformation launchInformation : launchInformations) {
			LOG.debug("Stopping endpoint {}", launchInformation);
			MasterEndpoint master = launchInformation.getMaster();
			master.stop();
		}
//		for (MasterLifecycle masterConnection : masterConnections) {
//			LOG.debug("Stopping master endpoint {}", masterConnection
//					.getClass().getCanonicalName());
//			masterConnection.stop();
//		}
		ShutdownWaitingService.await();
		undertowServer.stop();
	}
}
