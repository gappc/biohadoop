package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.BiohadoopConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterException;

public class AdapterLauncher {

	private static final Logger LOG = LoggerFactory
			.getLogger(AdapterLauncher.class);

	private final CommunicationConfiguration communicationConfiguration;
	private final List<LaunchInformation> launchInformations = new ArrayList<>();

	public AdapterLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	public void startAdapters() throws AdapterException {
		try {
			LOG.info("Adding default adapters");
			launchInformations.addAll(DefaultAdapterResolver
					.getDefaultAdapters(communicationConfiguration));

			LOG.info("Adding dedicated adapters");
			launchInformations.addAll(DedicatedAdapterResolver
					.getDedicatedAdapters(communicationConfiguration));

			if (launchInformations.size() == 0) {
				throw new AdapterException("No usable adapters found, maybe default adapters are overwritten in config file?");
			}

			LOG.info("Starting adapters");
			for (LaunchInformation launchInformation : launchInformations) {
				LOG.debug("Starting adapter {}", launchInformation);
				Adapter adapter = launchInformation.getAdapter();
				String pipelineName = launchInformation.getPipelineName();
				adapter.start(pipelineName);
			}
		} catch (ResolveDedicatedAdapterException e) {
			throw new AdapterException(e);
		}
	}

	public void stopAdapters() throws AdapterException {
		LOG.info("Stopping adapters");
		for (LaunchInformation launchInformation : launchInformations) {
			LOG.debug("Stopping adapter {}", launchInformation);
			Adapter adapter = launchInformation.getAdapter();
			adapter.stop();
		}
	}
}
