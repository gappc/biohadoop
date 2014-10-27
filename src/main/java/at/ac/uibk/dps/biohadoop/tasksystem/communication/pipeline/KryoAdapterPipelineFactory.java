package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import java.util.Map;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterInitialDataHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.AdapterWorkHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoObjectRegistrationHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoBuilder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoRegistrator;

import com.esotericsoftware.kryo.Kryo;

public class KryoAdapterPipelineFactory extends AbstractPipeline {

	public KryoAdapterPipelineFactory(ChannelGroup channels, String pipelineName) {
		super(channels, pipelineName);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = super.getPipeline();
		Kryo kryo = KryoBuilder.buildKryo(getKryoRegistrator());
		pipeline.addLast("kryoObjectRegistration", new KryoObjectRegistrationHandler());
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 1 * 1024, 2 * 1024 * 1024));
		pipeline.addLast("counter", counterHandler);
		pipeline.addLast("workHandler", new AdapterWorkHandler(pipelineName));
		pipeline.addLast("initialDataHandler", new AdapterInitialDataHandler(pipelineName));
//		pipeline.addLast("workHandler", new TestAdapterWorkHandler(pipelineName));
		return pipeline;
	}
	
	private KryoRegistrator getKryoRegistrator() throws AdapterException {
		String kryoRegistratorClassName = getKryoRegistratorClassName();
		if (kryoRegistratorClassName == null) {
			return null;
		}
		try {
			return (KryoRegistrator) Class.forName(kryoRegistratorClassName)
					.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | NoClassDefFoundError e) {
			throw new AdapterException(
					"Could not register objects for Kryo serialization, KryoRegistrator="
							+ kryoRegistratorClassName, e);
		}
	}

	
	private String getKryoRegistratorClassName()
			throws AdapterException {
		Map<String, String> properties = Environment
				.getBiohadoopConfiguration().getGlobalProperties();
		if (properties == null) {
			return null;
		}
		return properties.get(KryoRegistrator.KRYO_REGISTRATOR);
	}
}
