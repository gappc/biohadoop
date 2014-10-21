package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.worker.Worker;

public abstract class AbstractWorker implements Worker, ChannelPipelineFactory {
	private final Logger LOG = LoggerFactory
			.getLogger(getClass());

	private ChannelFactory factory;

	public void startClient(final String host, final int port, final ChannelPipelineFactory pipelineFactory) {
		factory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		LOG.info("{} Connecting to port {}", getClass().getSimpleName(), port);
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host,
				port));
		
		future.awaitUninterruptibly();
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
		}
		future.getChannel().getCloseFuture().awaitUninterruptibly();
		factory.releaseExternalResources();
	}
}
