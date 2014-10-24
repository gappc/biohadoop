package at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter;

import org.jboss.netty.channel.ChannelPipeline;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractAdapter;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterInitialDataHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterWorkHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.KryoWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.Worker;

import com.esotericsoftware.kryo.Kryo;

public class KryoAdapter extends AbstractAdapter {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		KryoObjectRegistration.registerDefaultObjects(kryo);
		ChannelPipeline pipeline = super.getPipeline();
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 1 * 1024, 2 * 1024 * 1024));
		pipeline.addLast("counter", counterHandler);
//		pipeline.addLast("pipelineExecutor", new ExecutionHandler(eventExecutor));
		pipeline.addLast("workHandler", new AdapterWorkHandler(pipelineName));
		pipeline.addLast("initialDataHandler", new AdapterInitialDataHandler(pipelineName));
//		pipeline.addLast("workHandler", new TestAdapterWorkHandler(pipelineName));
		return pipeline;
	}

	@Override
	protected Class<? extends Worker> getMatchingWorkerClass() {
		return KryoWorker.class;
	}

}
