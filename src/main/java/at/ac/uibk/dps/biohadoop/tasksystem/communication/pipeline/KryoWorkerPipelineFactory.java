package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.DefaultClientConnectionEstablishedHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoObjectRegistrationWorkerHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WorkerHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoObjectRegistration;

import com.esotericsoftware.kryo.Kryo;

public class KryoWorkerPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		KryoObjectRegistration.registerDefaultObjects(kryo);
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 1 * 1024,
				2 * 1024 * 1024));
		// We may need to register additional objects to Kryo. It is done
		// through this handler, that removes itself from the pipeline after the
		// registration is done
		pipeline.addLast("kryoObjectRegistration",
				new KryoObjectRegistrationWorkerHandler());
		// Temporary handler, that sends initial message. Removes itself from
		// pipeline after initial message is send
		pipeline.addLast("connectionEstablished",
				new DefaultClientConnectionEstablishedHandler());
		pipeline.addLast("counter", new CounterHandler());
		pipeline.addLast("worker", new WorkerHandler());
		// pipeline.addLast("worker", new TestWorkerHandler());
		return pipeline;
	}

}
