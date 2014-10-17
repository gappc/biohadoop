package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import org.jboss.netty.channel.ChannelPipelineFactory;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.KryoWorkerChannelPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.ConnectionRefusedException;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerException;

public class KryoWorker extends AbstractWorker {

	private final String host;
	private final int port;
	
	public KryoWorker(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	@Override
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void configure(String[] args) throws WorkerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws WorkerException, ConnectionRefusedException {
		ChannelPipelineFactory factory = new KryoWorkerChannelPipelineFactory();
		start(host, port, factory);
	}

}
