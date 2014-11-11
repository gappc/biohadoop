package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoBuilder;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoObjectRegistrationMessage;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoRegistrator;

public class KryoObjectRegistrationWorkerHandler extends
		SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (e.getMessage() instanceof KryoObjectRegistrationMessage) {
			KryoObjectRegistrationMessage message = (KryoObjectRegistrationMessage) e
					.getMessage();
			if (message.getClassName() != null) {
				ctx.getPipeline().remove(KryoEncoder.class);
				ctx.getPipeline().remove(KryoDecoder.class);

				KryoRegistrator kryoRegistrator = (KryoRegistrator) Class
						.forName(message.getClassName()).newInstance();

				ctx.getPipeline()
						.addBefore(
								"kryoObjectRegistration",
								"decoder",
								new KryoDecoder(KryoBuilder
										.buildKryo(kryoRegistrator)));
				ctx.getPipeline().addBefore(
						"kryoObjectRegistration",
						"encoder",
						new KryoEncoder(KryoBuilder.buildKryo(kryoRegistrator),
								message.getBufferSize(), message
										.getMaxBufferSize()));
			}
			ctx.getPipeline().remove(this);
		} else {
			super.messageReceived(ctx, e);
		}
	}

}
