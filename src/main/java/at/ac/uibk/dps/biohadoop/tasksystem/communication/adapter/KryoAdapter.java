package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import org.jboss.netty.channel.ChannelPipelineFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.KryoAdapterChannelPipelineFactory;

public class KryoAdapter extends AbstractAdapter {

	private String pipelineName;

	@Override
	public void configure(String pipelineName) throws AdapterException {
		this.pipelineName = pipelineName;
	}

	@Override
	public void start() throws AdapterException {
		CounterHandler counterHandler = new CounterHandler();
		ChannelPipelineFactory factory = new KryoAdapterChannelPipelineFactory(
				pipelineName, counterHandler);
		start(factory);
	}

}
