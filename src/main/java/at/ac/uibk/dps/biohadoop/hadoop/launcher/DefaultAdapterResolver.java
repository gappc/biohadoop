package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.KryoAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.WebSocketAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.SimpleTaskSubmitter;

public class DefaultAdapterResolver {

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<LaunchInformation> getDefaultAdapters(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> launchInformations = new ArrayList<>();

		LaunchInformation launchInformation = null;

		launchInformation = new LaunchInformation(new KryoAdapter());
		launchInformations.add(launchInformation);
		
		launchInformation = new LaunchInformation(new WebSocketAdapter());
		launchInformations.add(launchInformation);
		
		return launchInformations;
	}

}
