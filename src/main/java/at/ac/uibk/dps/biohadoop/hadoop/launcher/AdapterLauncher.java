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
	
	private List<Adapter> adapters = new ArrayList<>();

	public AdapterLauncher(BiohadoopConfiguration config) {
		communicationConfiguration = config.getCommunicationConfiguration();
	}

	public void startAdapters() throws AdapterException {
		try {
			LOG.info("Adding adapters");
			adapters = AdapterResolver.getAdapters(communicationConfiguration);

			if (adapters.size() == 0) {
				throw new AdapterException("No usable adapters found");
			}

			LOG.info("Starting adapters");
			for (Adapter adapter : adapters) {
				LOG.debug("Starting adapter {}", adapter);
				adapter.start();
			}
		} catch (AdapterLaunchException e) {
			throw new AdapterException(e);
		}
	}

	public void stopAdapters() throws AdapterException {
		LOG.info("Stopping adapters");
		for (Adapter adapter : adapters) {
			LOG.debug("Stopping adapter {}", adapter);
			adapter.stop();
		}
	}
}
