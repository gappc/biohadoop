package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoConfig;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoObjectRegistrationMessage;

public class KryoObjectRegistrationWorkerHandler extends
		SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (e.getMessage() instanceof KryoObjectRegistrationMessage) {
			KryoObjectRegistrationMessage message = (KryoObjectRegistrationMessage) e
					.getMessage();

			int bufferSize = message.getBufferSize() == 0 ? KryoConfig.KRYO_DEFAULT_BUFFER_SIZE
					: message.getBufferSize();
			int maxBufferSize = message.getMaxBufferSize() == 0 ? KryoConfig.KRYO_DEFAULT_MAX_BUFFER_SIZE
					: message.getMaxBufferSize();
			
			ctx.getPipeline().get(KryoEncoder.class).setBufferSizes(bufferSize, maxBufferSize);
		} else {
			super.messageReceived(ctx, e);
		}
	}

}
