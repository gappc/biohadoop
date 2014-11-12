package at.ac.uibk.dps.biohadoop.islandmodel.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.utils.HostInfo;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public class IslandModelServer {

	public static final String ISLAND_MODEL_HOST = "ISLAND_MODEL_HOST";
	public static final String ISLAND_MODEL_PORT = "ISLAND_MODEL_PORT";
	
	private static final Logger LOG = LoggerFactory
			.getLogger(IslandModelServer.class);

	private final ChannelGroup channels = new DefaultChannelGroup(this
			.getClass().getCanonicalName() + "-server");
	
	private static AtomicBoolean running = new AtomicBoolean(false);
	
	private ChannelFactory factory;
	
	public void startServer(final ChannelPipelineFactory pipelineFactory) {
		if (running.getAndSet(true)) {
			return;
		}
		factory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setPipelineFactory(pipelineFactory);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		PortFinder.aquireBindingLock();
		int port = PortFinder.findFreePort(30000);
		Channel channel = bootstrap.bind(new InetSocketAddress(port));
		LOG.info("{} binding to port {}", getClass().getSimpleName(), port);
		PortFinder.releaseBindingLock();

		channels.add(channel);
		
		Environment.set(ISLAND_MODEL_HOST, HostInfo.getHostname());
		Environment.set(ISLAND_MODEL_PORT, Integer.toString(port));
	}
	
	public void stopServer() {
		LOG.info("Stopping {} channels", channels.size());
		ChannelGroupFuture future = channels.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();
	}
	
}
