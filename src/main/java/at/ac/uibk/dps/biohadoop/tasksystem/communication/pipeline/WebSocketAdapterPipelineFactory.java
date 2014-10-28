package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterInitialDataHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterWorkHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CustomWebSocketServerProtocolHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WebSocketEncoder;

public class WebSocketAdapterPipelineFactory extends AbstractPipeline {

	public WebSocketAdapterPipelineFactory(ChannelGroup channels, String pipelineName) {
		super(channels, pipelineName);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = super.getPipeline();
		pipeline.addLast("httpDecoder", new HttpRequestDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("protocolHandler",
				new CustomWebSocketServerProtocolHandler("/ws"));
		pipeline.addLast("webSocketDecoder", new WebSocketDecoder());
		pipeline.addLast("httpEncoder", new HttpResponseEncoder());
		pipeline.addLast("webSocketEncoder", new WebSocketEncoder());
		pipeline.addLast("counter", counterHandler);
		pipeline.addLast("workHandler", new AdapterWorkHandler(pipelineName));
		pipeline.addLast("initialDataHandler", new AdapterInitialDataHandler(pipelineName));
		return pipeline;
	}
}
