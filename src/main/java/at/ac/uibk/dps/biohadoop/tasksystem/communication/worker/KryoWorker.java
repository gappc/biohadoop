package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import org.jboss.netty.channel.ChannelPipelineFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.KryoWorkerPipelineFactory;

public class KryoWorker extends AbstractWorkerComm {

	@Override
	public void start(String host, int port) throws WorkerException, ConnectionRefusedException {
		ChannelPipelineFactory pipelineFactory = new KryoWorkerPipelineFactory();
		client.startClient(host, port, pipelineFactory);
	}

}
