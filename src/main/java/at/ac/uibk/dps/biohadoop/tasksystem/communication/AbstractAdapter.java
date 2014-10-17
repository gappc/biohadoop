package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public abstract class AbstractAdapter implements Adapter {

	public final ChannelGroup CHANNELS = new DefaultChannelGroup(this
			.getClass().getCanonicalName() + "-server");

	
	private final Logger LOG = LoggerFactory
			.getLogger(getClass());
	private static final int PORT_RANGE_LOW = 30000;

	private ChannelFactory factory;

	public void start(final ChannelPipelineFactory pipelineFactory) {
		factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		PortFinder.aquireBindingLock();
		int port = PortFinder.findFreePort(PORT_RANGE_LOW);
		Channel channel = bootstrap.bind(new InetSocketAddress(port));
		LOG.info("{} binding to port {}", getClass().getSimpleName(), port);
		PortFinder.releaseBindingLock();

		CHANNELS.add(channel);
	}

	public void stop() {
		ChannelGroupFuture future = CHANNELS.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();
	}
}
