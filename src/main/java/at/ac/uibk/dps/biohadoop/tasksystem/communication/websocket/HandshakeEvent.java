package at.ac.uibk.dps.biohadoop.tasksystem.communication.websocket;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.Channels;

public class HandshakeEvent implements ChannelEvent {

	private final Channel channel;
	
	public HandshakeEvent(Channel channel) {
		this.channel = channel;
	}

	@Override
	public Channel getChannel() {
		return channel;
	}

	@Override
	public ChannelFuture getFuture() {
		return Channels.succeededFuture(this.channel);
	}

}
