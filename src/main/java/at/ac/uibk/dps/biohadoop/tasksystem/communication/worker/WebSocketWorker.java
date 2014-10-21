package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketClientConnectionEstablishedHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketHandshakeHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WorkerHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.ConnectionRefusedException;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerException;

public class WebSocketWorker extends AbstractWorker {

	private WebSocketClientHandshaker handshaker;

	@Override
	public void start(String host, int port) throws WorkerException, ConnectionRefusedException {
		try {
			URI uri = new URI("ws", null, host, port, "/ws", null, null);
			handshaker = new WebSocketClientHandshakerFactory()
					.newHandshaker(uri, WebSocketVersion.V13, null, false, null);
			startClient(host, port, this);
		} catch (URISyntaxException e) {
			throw new WorkerException("Wrong URI for host " + host
					+ " and port " + port, e);
		}
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("httpDecoder", new HttpResponseDecoder());
		pipeline.addLast("httpEncoder", new HttpRequestEncoder());
		// Temporary handler, that is removed after the Websocket upgrade has
		// been completed
		pipeline.addLast("handshake", new WebSocketHandshakeHandler(handshaker));
		// Temporary handler, that sends initial message. Removes itself from
		// pipeline after initial message is send
		pipeline.addLast("connectionEstablished",
				new WebSocketClientConnectionEstablishedHandler());
		pipeline.addLast("counter", new CounterHandler());
		pipeline.addLast("worker", new WorkerHandler());
		return pipeline;
	}

}
