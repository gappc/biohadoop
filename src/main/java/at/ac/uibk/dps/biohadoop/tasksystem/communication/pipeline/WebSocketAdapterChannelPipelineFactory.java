package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CustomWebSocketServerProtocolHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.QueueHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketEncoder;

public class WebSocketAdapterChannelPipelineFactory implements
		ChannelPipelineFactory {

	private final String pipelineName;
	private final CounterHandler counterHandler;

	public WebSocketAdapterChannelPipelineFactory(String pipelineName,
			CounterHandler counterHandler) {
		this.pipelineName = pipelineName;
		this.counterHandler = counterHandler;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("httpDecoder", new HttpRequestDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("protocolHandler",
				new CustomWebSocketServerProtocolHandler("/ws"));
		pipeline.addLast("webSocketDecoder", new WebSocketDecoder());
		pipeline.addLast("httpEncoder", new HttpResponseEncoder());
		pipeline.addLast("webSocketEncoder", new WebSocketEncoder());
		pipeline.addLast("counter", counterHandler);
		pipeline.addLast("queueHandler", new QueueHandler(pipelineName));

		return pipeline;
	}

}
