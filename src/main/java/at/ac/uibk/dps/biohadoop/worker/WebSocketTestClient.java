package at.ac.uibk.dps.biohadoop.worker;

import java.net.URI;

public class WebSocketTestClient {

	public static void main(String[] args) throws Exception {
		WebSocketClient client = new WebSocketClient(URI.create("ws://kleintroppl:30000/websocket/ga"));
	}

}
