package at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.KryoEndpointPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.KryoWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;

public class KryoEndpoint extends AbstractEndpoint {

	@Override
	public void start() throws EndpointException {
		ChannelGroup channels = server.getChannelGroup();
		ChannelPipelineFactory pipelineFactory = new KryoEndpointPipelineFactory(channels);
		server.startServer(pipelineFactory, getMatchingWorkerClass());
	}

	@Override
	public Class<? extends Worker> getMatchingWorkerClass() {
		return KryoWorker.class;
	}

}
