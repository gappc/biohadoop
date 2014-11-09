package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.worker.Worker;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public class NettyServer {

	protected final ChannelGroup channels = new DefaultChannelGroup(this
			.getClass().getCanonicalName() + "-server");

	protected CounterHandler counterHandler = new CounterHandler();

	private static final int PORT_RANGE_LOW = 30000;
	private static final Map<String, Integer> PORT_MAP = new ConcurrentHashMap<>();
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private ChannelFactory factory;

	public static Integer getPort(Class<? extends Worker> workerClass) {
		return PORT_MAP.get(workerClass.getCanonicalName());
	}
	
	public ChannelGroup getChannelGroup() {
		return channels;
	}
	
	public void startServer(final ChannelPipelineFactory pipelineFactory, Class<? extends Worker> workerClass) {
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

		channels.add(channel);
		
		registerPort(workerClass, port);
	}
	
	public void stopServer() {
		LOG.info("Stopping {} channels", channels.size());
		ChannelGroupFuture future = channels.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();
	}
	
	private void registerPort(Class<? extends Worker> workerClass, int port) {
		String key = workerClass.getCanonicalName();
		PORT_MAP.put(key, port);
	}

}
