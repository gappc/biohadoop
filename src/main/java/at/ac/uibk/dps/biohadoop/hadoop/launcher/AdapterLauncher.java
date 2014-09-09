package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.shutdown.ShutdownWaitingService;
import at.ac.uibk.dps.biohadoop.tasksystem.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.utils.launch.DedicatedRemoteExecutableResolver;
import at.ac.uibk.dps.biohadoop.utils.launch.DefaultRemoteExecutableResolver;
import at.ac.uibk.dps.biohadoop.utils.launch.LaunchInformation;
import at.ac.uibk.dps.biohadoop.utils.launch.ResolveDedicatedAdapterException;
import at.ac.uibk.dps.biohadoop.webserver.StartServerException;
import at.ac.uibk.dps.biohadoop.webserver.UndertowServer;

public class AdapterLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(AdapterLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	private final List<LaunchInformation> launchInformations = new ArrayList<>();
	private UndertowServer undertowServer;

	public AdapterLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	public void startAdapters() throws AdapterException {
		try {
			LOG.info("Adding default adapters");
			launchInformations.addAll(DefaultRemoteExecutableResolver
					.getDefaultAdapters(communicationConfiguration));

			LOG.info("Adding dedicated adapters");
			launchInformations.addAll(DedicatedRemoteExecutableResolver
					.getDedicatedAdapters(communicationConfiguration));

			if (launchInformations.size() == 0) {
				LOG.warn("No usable adapters found, maybe default adapters are overwritten in config file?");
			}

			LOG.info("Configuring adapters");
			for (LaunchInformation launchInformation : launchInformations) {
				LOG.debug("Configuring adapter {}", launchInformation);
				Adapter adapter = launchInformation.getAdapter();
				String pipelineName = launchInformation.getPipelineName();
				adapter.configure(pipelineName);
			}

			undertowServer = new UndertowServer();
			undertowServer.start();

			LOG.info("Starting adapters");
			for (LaunchInformation launchInformation : launchInformations) {
				LOG.debug("Starting adapter {}", launchInformation);
				Adapter adapter = launchInformation.getAdapter();
				adapter.start();
			}
		} catch (StartServerException e) {
			throw new AdapterException(e);
		} catch (ResolveDedicatedAdapterException e) {
			throw new AdapterException(e);
		}
	}

	// TODO: what happens if any adapter throws exception?
	public void stopAdapters() throws Exception {
		LOG.info("Stopping adapters");
		for (LaunchInformation launchInformation : launchInformations) {
			LOG.debug("Stopping adapter {}", launchInformation);
			Adapter adapter = launchInformation.getAdapter();
			adapter.stop();
		}
		ShutdownWaitingService.await();
		undertowServer.stop();
	}
}
