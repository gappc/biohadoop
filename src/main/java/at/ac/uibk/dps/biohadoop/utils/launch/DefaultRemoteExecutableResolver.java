package at.ac.uibk.dps.biohadoop.utils.launch;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.DefaultKryoEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.local.DefaultLocalEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.rest.DefaultRestEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.socket.DefaultSocketEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.DefaultWebSocketEndpoint;
import at.ac.uibk.dps.biohadoop.communication.worker.DefaultLocalWorker;
import at.ac.uibk.dps.biohadoop.queue.SimpleTaskSubmitter;

public class DefaultRemoteExecutableResolver {

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<LaunchInformation> getDefaultEndpoints(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> defaultEndpoints = new ArrayList<>();

		LaunchInformation launchInformation = null;

		defaultEndpoints.addAll(getLocalEndpoints(communicationConfiguration));

		launchInformation = new LaunchInformation(null,
				new DefaultKryoEndpoint(), SimpleTaskSubmitter.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultRestEndpoint<>(), SimpleTaskSubmitter.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultSocketEndpoint(), SimpleTaskSubmitter.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultWebSocketEndpoint<>(), SimpleTaskSubmitter.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		return defaultEndpoints;
	}

	public static List<LaunchInformation> getLocalEndpoints(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> launchInformations = new ArrayList<LaunchInformation>();
		for (WorkerConfiguration workerConfiguration : communicationConfiguration
				.getWorkerConfigurations()) {
			if (DefaultLocalWorker.class
					.equals(workerConfiguration.getWorker())) {
				Integer count = workerConfiguration.getCount();
				for (int i = 0; i < count; i++) {
					launchInformations.add(new LaunchInformation(null,
							new DefaultLocalEndpoint(),
							SimpleTaskSubmitter.QUEUE_NAME));
				}
			}
		}
		return launchInformations;
	}

}
