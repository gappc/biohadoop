package at.ac.uibk.dps.biohadoop.tasksystem.communication.worker;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.AbstractWorker;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.pipeline.WebSocketWorkerChannelPipelineFactory;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.ConnectionRefusedException;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.WorkerException;

public class WebSocketWorker extends AbstractWorker {

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
		String host = "localhost";
		int port = 30000;
		try {
			URI uri = new URI("ws", null, host, port, "/ws", null, null);
			final WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory()
					.newHandshaker(uri, WebSocketVersion.V13, null, false, null);
			ChannelPipelineFactory factory = new WebSocketWorkerChannelPipelineFactory(
					handshaker);
			start("localhost", 30000, factory);
		} catch (URISyntaxException e) {
			throw new WorkerException("Wrong URI for host " + host
					+ " and port " + port, e);
		}
	}

}
