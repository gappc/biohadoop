package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.EndpointException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.WebSocketEndpoint;

public class TestEndpoint {

	public static void main(String[] args) throws EndpointException {
		WebSocketEndpoint endpoint = new WebSocketEndpoint();
//		KryoEndpoint endpoint = new KryoEndpoint();
		endpoint.start();
	}

}
