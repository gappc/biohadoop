package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import java.util.Map;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.endpoint.EndpointException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.EndpointInitialDataHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.EndpointWorkHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoObjectRegistrationHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoBuilder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoConfig;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoRegistrator;

import com.esotericsoftware.kryo.Kryo;

public class KryoEndpointPipelineFactory extends AbstractPipeline {

	public KryoEndpointPipelineFactory(ChannelGroup channels) {
		super(channels);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = super.getPipeline();
		Kryo kryo = KryoBuilder.buildKryo(getKryoRegistrator());
		pipeline.addLast("kryoObjectRegistration",
				new KryoObjectRegistrationHandler());
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast(
				"encoder",
				new KryoEncoder(kryo, KryoConfig.getBufferSize(), KryoConfig
						.getMaxBufferSize()));
		pipeline.addLast("counter", counterHandler);
		pipeline.addLast("workHandler", new EndpointWorkHandler());
		pipeline.addLast("initialDataHandler", new EndpointInitialDataHandler());
		// pipeline.addLast("workHandler", new
		// TestEndpointWorkHandler(pipelineName));
		return pipeline;
	}

	private KryoRegistrator getKryoRegistrator() throws EndpointException {
		String kryoRegistratorClassName = getKryoRegistratorClassName();
		if (kryoRegistratorClassName == null) {
			return null;
		}
		try {
			return (KryoRegistrator) Class.forName(kryoRegistratorClassName)
					.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | NoClassDefFoundError e) {
			throw new EndpointException(
					"Could not register objects for Kryo serialization, KryoRegistrator="
							+ kryoRegistratorClassName, e);
		}
	}

	private String getKryoRegistratorClassName() throws EndpointException {
		Map<String, String> properties = Environment
				.getBiohadoopConfiguration().getGlobalProperties();
		if (properties == null) {
			return null;
		}
		return properties.get(KryoRegistrator.KRYO_REGISTRATOR);
	}
}
