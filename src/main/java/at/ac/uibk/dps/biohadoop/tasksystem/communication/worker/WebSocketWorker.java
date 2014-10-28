package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.WebSocketWorkerPipelineFactory;

public class WebSocketWorker extends AbstractWorker {

	@Override
	public void start(String host, int port) throws WorkerException,
			ConnectionRefusedException {
		WebSocketClientHandshaker handshaker = getHandshaker(host, port);
		ChannelPipelineFactory pipelineFactory = new WebSocketWorkerPipelineFactory(
				handshaker);
		client.startClient(host, port, pipelineFactory);
	}

	private WebSocketClientHandshaker getHandshaker(String host, int port)
			throws WorkerException {
		try {
			URI uri = new URI("ws", null, host, port, "/ws", null, null);
			return new WebSocketClientHandshakerFactory().newHandshaker(uri,
					WebSocketVersion.V13, null, false, null);
		} catch (URISyntaxException e) {
			throw new WorkerException("Wrong URI for host " + host
					+ " and port " + port, e);
		}
	}

}
