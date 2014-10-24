package at.ac.uibk.dps.biohadoop.tasksystem.communication;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.tasksystem.adapter.Adapter;
import at.ac.uibk.dps.biohadoop.tasksystem.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.ChannelGroupHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.handler.CounterHandler;
import at.ac.uibk.dps.biohadoop.tasksystem.worker.Worker;
import at.ac.uibk.dps.biohadoop.utils.PortFinder;

public abstract class AbstractAdapter implements Adapter,
		ChannelPipelineFactory {

	protected final ChannelGroup channels = new DefaultChannelGroup(this
			.getClass().getCanonicalName() + "-server");

	protected String pipelineName;
	protected CounterHandler counterHandler = new CounterHandler();

	private static final int PORT_RANGE_LOW = 30000;
	private static final Map<String, Integer> PORT_MAP = new ConcurrentHashMap<>();
	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private ChannelFactory factory;

	public static Integer getPort(Class<? extends Worker> workerClass, String pipelineName) {
		return PORT_MAP.get(workerClass.getCanonicalName() + "-" + pipelineName);
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("channelGroup", new ChannelGroupHandler(channels));
		return pipeline;
	}
	
	@Override
	public void start(String pipelineName) throws AdapterException {
		this.pipelineName = pipelineName;
		startServer(this);
	}

	@Override
	public void stop() throws AdapterException {
		LOG.info("Stopping {} channels", channels.size());
		ChannelGroupFuture future = channels.close();
		future.awaitUninterruptibly();
		factory.releaseExternalResources();
	}
	
	protected abstract Class<? extends Worker> getMatchingWorkerClass();
	
	private void startServer(final ChannelPipelineFactory pipelineFactory) {
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
		
		registerPort(port);
	}
	
	private void registerPort(int port) {
		String key = getMatchingWorkerClass().getCanonicalName() + "-" + pipelineName;
		PORT_MAP.put(key, port);
	}
}
