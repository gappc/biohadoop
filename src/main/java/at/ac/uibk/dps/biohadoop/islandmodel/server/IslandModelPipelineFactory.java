package at.ac.uibk.dps.biohadoop.islandmodel.server;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class IslandModelPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("httpDecoder", new HttpRequestDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(65536));
		pipeline.addLast("httpEncoder", new HttpResponseEncoder());
		pipeline.addLast("islandDataHandler", new IslandModelDataHandler());
		return pipeline;
	}

}
