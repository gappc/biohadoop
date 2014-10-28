package at.ac.uibk.dps.biohadoop.utils.launch;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.tasksystem.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.local.LocalAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.KryoAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.WebSocketAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.LocalWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.submitter.SimpleTaskSubmitter;

public class DefaultAdapterResolver {

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<LaunchInformation> getDefaultAdapters(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> launchInformations = new ArrayList<>();

		LaunchInformation launchInformation = null;

		launchInformations.addAll(getLocalAdapters(communicationConfiguration));

		launchInformation = new LaunchInformation(new KryoAdapter(),
				SimpleTaskSubmitter.PIPELINE_NAME);
		launchInformations.add(launchInformation);
		
		launchInformation = new LaunchInformation(new WebSocketAdapter(),
				SimpleTaskSubmitter.PIPELINE_NAME);
		launchInformations.add(launchInformation);
		
		return launchInformations;
	}

	private static List<LaunchInformation> getLocalAdapters(
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
							SimpleTaskSubmitter.PIPELINE_NAME));
				}
			}
		}
		return launchInformations;
	}

}
