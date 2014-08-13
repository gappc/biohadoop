package at.ac.uibk.dps.biohadoop.utils.launch;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.DefaultKryoServer;
import at.ac.uibk.dps.biohadoop.communication.master.local.DefaultLocalMasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.rest.DefaultRestEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.socket.DefaultSocketConnection;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.DefaultWebSocketMaster;
import at.ac.uibk.dps.biohadoop.communication.worker.DefaultLocalWorker;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;

public class DefaultRemoteExecutableResolver {

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<LaunchInformation> getDefaultEndpoints(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> defaultEndpoints = new ArrayList<>();

		LaunchInformation launchInformation = null;

		defaultEndpoints.addAll(getLocalEndpoints(communicationConfiguration));

		launchInformation = new LaunchInformation(null,
				new DefaultKryoServer(), DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultRestEndpoint(), DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultSocketConnection(), DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultWebSocketMaster(), DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		return defaultEndpoints;
	}

	public static List<LaunchInformation> getLocalEndpoints(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> launchInformations = new ArrayList<LaunchInformation>();
		for (WorkerConfiguration workerConfiguration : communicationConfiguration
				.getWorkerConfigurations()) {
			if (DefaultLocalWorker.class
					.equals(workerConfiguration.getWorker())
					&& workerConfiguration.getRemoteExecutable() == null) {
				Integer count = workerConfiguration.getCount();
				for (int i = 0; i < count; i++) {
					launchInformations.add(new LaunchInformation(null,
							new DefaultLocalMasterEndpoint(),
							DefaultTaskClient.QUEUE_NAME));
				}
			}
		}
		return launchInformations;
	}

}
