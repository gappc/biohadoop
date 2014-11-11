package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.adapter.AdapterException;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoConfig;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoObjectRegistrationMessage;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.kryo.KryoRegistrator;

public class KryoObjectRegistrationHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		String className = getKryoRegistratorClassName();
		int bufferSize = KryoConfig.getBufferSize();
		int maxBufferSize = KryoConfig.getMaxBufferSize();
		e.getChannel().write(
				new KryoObjectRegistrationMessage(className, bufferSize,
						maxBufferSize));
		super.channelConnected(ctx, e);
	}

	private String getKryoRegistratorClassName() throws AdapterException {
		Map<String, String> properties = Environment
				.getBiohadoopConfiguration().getGlobalProperties();
		if (properties == null) {
			return null;
		}
		return properties.get(KryoRegistrator.KRYO_REGISTRATOR);
	}

}
