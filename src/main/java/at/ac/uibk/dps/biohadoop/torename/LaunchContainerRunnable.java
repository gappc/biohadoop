package at.ac.uibk.dps.biohadoop.torename;

import java.io.IOException;

import org.apache.hadoop.yarn.api.records.Container;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaunchContainerRunnable implements Runnable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LaunchContainerRunnable.class);

	private NMClient nmClient;
	private Container container;
	private ContainerLaunchContext ctx;

	public LaunchContainerRunnable(NMClient nmClient, Container container,
			ContainerLaunchContext ctx) {
		this.nmClient = nmClient;
		this.container = container;
		this.ctx = ctx;
	}

	@Override
	public void run() {
		try {
			nmClient.startContainer(container, ctx);
		} catch (YarnException e) {
			LOGGER.error("Error while starting container {}", container.getId(), e);
		} catch (IOException e) {
			LOGGER.error("Error while starting container {}", container.getId(), e);
		}
	}

}
