package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CounterHandler extends SimpleChannelUpstreamHandler {
	
	private static final Logger LOG = LoggerFactory
			.getLogger(CounterHandler.class);
	
	private final AtomicInteger count = new AtomicInteger();
	private long start = System.nanoTime();

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (count.incrementAndGet() % 100000 == 0) {
			LOG.info(count.get() + " messages received");
			long end = System.nanoTime();
			LOG.info("{}", end - start);
			start = end;
		}
		super.messageReceived(ctx, e);
	}
	
}
