package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketClientConnectionEstablishedHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketHandshakeHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WorkerHandler;

public class WebSocketWorkerChannelPipelineFactory implements
		ChannelPipelineFactory {

	private final WebSocketClientHandshaker handshaker;

	public WebSocketWorkerChannelPipelineFactory(
			WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("httpDecoder", new HttpResponseDecoder());
		pipeline.addLast("httpEncoder", new HttpRequestEncoder());
		// Temporary handler, that is removed after the Websocket upgrade has
		// been completed
		pipeline.addLast("handshake", new WebSocketHandshakeHandler(handshaker));
		// Temporary handler, that is removed after initial message is send
		pipeline.addLast("connectionEstablished",
				new WebSocketClientConnectionEstablishedHandler());
		pipeline.addLast("counter", new CounterHandler());
		pipeline.addLast("worker", new WorkerHandler());
		return pipeline;
	}

}
