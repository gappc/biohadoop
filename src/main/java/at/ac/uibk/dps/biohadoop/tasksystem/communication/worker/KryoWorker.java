package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import org.jboss.netty.channel.ChannelPipelineFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.KryoWorkerPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.ConnectionRefusedException;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerException;

public class KryoWorker extends AbstractWorker {

	@Override
	public void start(String host, int port) throws WorkerException, ConnectionRefusedException {
		ChannelPipelineFactory pipelineFactory = new KryoWorkerPipelineFactory();
		client.startClient(host, port, pipelineFactory);
	}

}
