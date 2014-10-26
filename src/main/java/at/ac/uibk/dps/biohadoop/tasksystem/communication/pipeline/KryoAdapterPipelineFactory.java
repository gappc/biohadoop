package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterInitialDataHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterWorkHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoObjectRegistrationHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoObjectRegistration;

import com.esotericsoftware.kryo.Kryo;

public class KryoAdapterPipelineFactory extends AbstractPipeline {

	public KryoAdapterPipelineFactory(ChannelGroup channels, String pipelineName) {
		super(channels, pipelineName);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		KryoObjectRegistration.registerDefaultObjects(kryo);
		ChannelPipeline pipeline = super.getPipeline();
		pipeline.addLast("kryoObjectRegistration", new KryoObjectRegistrationHandler());
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 1 * 1024, 2 * 1024 * 1024));
		pipeline.addLast("counter", counterHandler);
//		pipeline.addLast("pipelineExecutor", new ExecutionHandler(eventExecutor));
		pipeline.addLast("workHandler", new AdapterWorkHandler(pipelineName));
		pipeline.addLast("initialDataHandler", new AdapterInitialDataHandler(pipelineName));
//		pipeline.addLast("workHandler", new TestAdapterWorkHandler(pipelineName));
		return pipeline;
	}
}
