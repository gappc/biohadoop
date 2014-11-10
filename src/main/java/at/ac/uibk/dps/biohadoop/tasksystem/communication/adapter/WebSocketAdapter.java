package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.WebSocketAdapterPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.WebSocketWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;

public class WebSocketAdapter extends AbstractAdapter {

	@Override
	public void start() throws AdapterException {
		ChannelGroup channels = server.getChannelGroup();
		ChannelPipelineFactory pipelineFactory = new WebSocketAdapterPipelineFactory(channels);
		server.startServer(pipelineFactory, getMatchingWorkerClass());
	}
	
	@Override
	public Class<? extends Worker> getMatchingWorkerClass() {
		return WebSocketWorker.class;
	}

}