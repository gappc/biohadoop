package at.ac.uibk.dps.biohadoop.utils.launch;

import java.util.ArrayList;
import java.util.List;

import at.ac.uibk.dps.biohadoop.communication.CommunicationConfiguration;
import at.ac.uibk.dps.biohadoop.communication.master.kryo.DefaultKryoServer;
import at.ac.uibk.dps.biohadoop.communication.master.local.LocalMasterEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.rest.DefaultRestEndpoint;
import at.ac.uibk.dps.biohadoop.communication.master.socket.DefaultSocketConnection;
import at.ac.uibk.dps.biohadoop.communication.master.websocket.DefaultWebSocketMaster;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;

public class DefaultRemoteExecutableResolver {

	// TODO Consider CommunicationConfiguration, such that default values can be
	// overridden
	public static List<LaunchInformation> getDefaultEndpoints(
			CommunicationConfiguration communicationConfiguration) {
		List<LaunchInformation> defaultEndpoints = new ArrayList<>();

		LaunchInformation launchInformation = null;

		launchInformation = new LaunchInformation(null, new LocalMasterEndpoint(),
				DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);
		
		launchInformation = new LaunchInformation(null, new DefaultKryoServer(),
				DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultRestEndpoint(), DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null,
				new DefaultSocketConnection(), DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		launchInformation = new LaunchInformation(null, new DefaultWebSocketMaster(),
				DefaultTaskClient.QUEUE_NAME);
		defaultEndpoints.add(launchInformation);

		return defaultEndpoints;
	}

}
