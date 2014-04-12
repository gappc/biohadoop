package at.ac.uibk.dps.biohadoop.ga.worker;

import java.net.URI;

public class WebSocketClientMain {

	public static void main(String[] args) throws Exception {
		WebSocketClient client = new WebSocketClient(URI.create("ws://kleintroppl:30000/websocket/ga"));
	}

}
