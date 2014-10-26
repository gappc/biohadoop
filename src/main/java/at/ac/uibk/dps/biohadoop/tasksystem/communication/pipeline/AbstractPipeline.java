package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.ChannelGroupHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;

public abstract class AbstractPipeline implements ChannelPipelineFactory {

	private final ChannelGroup channels;
	protected final String pipelineName;
	protected final CounterHandler counterHandler;
	
	public AbstractPipeline(ChannelGroup channels, String pipelineName) {
		this.channels = channels;
		this.pipelineName = pipelineName;
		this.counterHandler = new CounterHandler();
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("channelGroup", new ChannelGroupHandler(channels));
		return pipeline;
	}
	
}
