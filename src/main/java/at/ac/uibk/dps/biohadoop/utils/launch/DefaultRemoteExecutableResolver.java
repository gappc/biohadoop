package at.ac.uibk.dps.biohadoop.utils.launch;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.adapter.kryo.KryoAdapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.local.LocalAdapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.rest.RestAdapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.socket.SocketAdapter;
import at.ac.uibk.dps.biohadoop.communication.adapter.websocket.WebSocketAdapter;
import at.ac.uibk.dps.biohadoop.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.communication.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.queue.SimpleTaskSubmitter;

public class DefaultRemoteExecutableResolver {

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<LaunchInformation> getDefaultAdapters(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> launchInformations = new ArrayList<>();

		LaunchInformation launchInformation = null;

		launchInformations.addAll(getLocalAdapters(communicationConfiguration));

		launchInformation = new LaunchInformation(new KryoAdapter(),
				SimpleTaskSubmitter.SETTING_NAME);
		launchInformations.add(launchInformation);

		launchInformation = new LaunchInformation(new RestAdapter<>(),
				SimpleTaskSubmitter.SETTING_NAME);
		launchInformations.add(launchInformation);

		launchInformation = new LaunchInformation(new SocketAdapter(),
				SimpleTaskSubmitter.SETTING_NAME);
		launchInformations.add(launchInformation);

		launchInformation = new LaunchInformation(
				new WebSocketAdapter<>(),
				SimpleTaskSubmitter.SETTING_NAME);
		launchInformations.add(launchInformation);

		return launchInformations;
	}

	public static List<LaunchInformation> getLocalAdapters(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> launchInformations = new ArrayList<LaunchInformation>();
		for (WorkerConfiguration workerConfiguration : communicationConfiguration
				.getWorkerConfigurations()) {
			if (LocalWorker.class
					.equals(workerConfiguration.getWorker())) {
				Integer count = workerConfiguration.getCount();
				for (int i = 0; i < count; i++) {
					launchInformations.add(new LaunchInformation(
							new LocalAdapter(),
							SimpleTaskSubmitter.SETTING_NAME));
				}
			}
		}
		return launchInformations;
	}

}
