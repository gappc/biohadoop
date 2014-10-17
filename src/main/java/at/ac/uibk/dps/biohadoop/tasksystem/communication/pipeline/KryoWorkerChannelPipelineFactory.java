package at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;

import at.ac.uibk.dps.biohadoop.tasksystem.Message;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.DefaultClientConnectionEstablishedHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WorkerHandler;

import com.esotericsoftware.kryo.Kryo;

public class KryoWorkerChannelPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		kryo.register(Message.class);
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 4 * 1024, 16 * 1024));
		// Temporary handler, that is removed after initial message is send
		pipeline.addLast("connectionEstablished",
				new DefaultClientConnectionEstablishedHandler());
		pipeline.addLast("counter", new CounterHandler());
		pipeline.addLast("worker", new WorkerHandler());
		return pipeline;
	}

}
