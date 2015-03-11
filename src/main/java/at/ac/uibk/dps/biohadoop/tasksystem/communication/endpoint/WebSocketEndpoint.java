package at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.WebSocketEndpointPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WebSocketWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WorkerComm;

public class WebSocketEndpoint extends AbstractEndpoint {

	@Override
	public void start() throws EndpointException {
		ChannelGroup channels = server.getChannelGroup();
		ChannelPipelineFactory pipelineFactory = new WebSocketEndpointPipelineFactory(channels);
		server.startServer(pipelineFactory, getMatchingWorkerClass());
	}
	
	@Override
	public Class<? extends WorkerComm> getMatchingWorkerClass() {
		return WebSocketWorker.class;
	}

}
