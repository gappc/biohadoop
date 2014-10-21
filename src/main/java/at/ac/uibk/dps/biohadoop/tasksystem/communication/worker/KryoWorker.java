package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.kryo.KryoObjectRegistration;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.DefaultClientConnectionEstablishedHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoDecoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.KryoEncoder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.TestWorkerHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.WorkerHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.ConnectionRefusedException;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerException;

import com.esotericsoftware.kryo.Kryo;

public class KryoWorker extends AbstractWorker {

	@Override
	public void start(String host, int port) throws WorkerException, ConnectionRefusedException {
		startClient(host, port, this);
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		Kryo kryo = new Kryo();
		kryo.setReferences(false);
		KryoObjectRegistration.registerDefaultObjects(kryo);
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new KryoDecoder(kryo));
		pipeline.addLast("encoder", new KryoEncoder(kryo, 1 * 1024, 512 * 1024));
		// Temporary handler, that sends initial message. Removes itself from
		// pipeline after initial message is send
		pipeline.addLast("connectionEstablished",
				new DefaultClientConnectionEstablishedHandler());
		pipeline.addLast("counter", new CounterHandler());
		pipeline.addLast("worker", new WorkerHandler());
//		pipeline.addLast("worker", new TestWorkerHandler());
		return pipeline;
	}
}
