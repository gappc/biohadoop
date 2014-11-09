package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.KryoAdapterPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.KryoWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;

public class KryoAdapter extends AbstractAdapter {

	@Override
	public void start() throws AdapterException {
		ChannelGroup channels = server.getChannelGroup();
		ChannelPipelineFactory pipelineFactory = new KryoAdapterPipelineFactory(channels);
		server.startServer(pipelineFactory, getMatchingWorkerClass());
	}

	@Override
	public Class<? extends Worker> getMatchingWorkerClass() {
		return KryoWorker.class;
	}

}
