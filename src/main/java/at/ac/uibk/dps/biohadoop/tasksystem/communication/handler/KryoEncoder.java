package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeOutput;

/**
 * Taken from https://github.com/EsotericSoftware/kryonetty/blob/master/src/com/esotericsoftware/kryonetty/KryoChannelPipelineFactory.java
 * 
 * @author Christian Gapp
 *
 */
public class KryoEncoder extends OneToOneEncoder {
	private Output output;
	private final Kryo kryo;

	public KryoEncoder(Kryo kryo, int bufferSize, int maxBufferSize) {
		this.kryo = kryo;
		output = new UnsafeOutput(bufferSize, maxBufferSize);
	}

	public void setBufferSizes(int bufferSize, int maxBufferSize) {
		output = new UnsafeOutput(bufferSize, maxBufferSize);
	}
	
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object object) throws Exception {
		output.clear();
		output.setPosition(4);
		kryo.writeClassAndObject(output, object);
		int total = output.position();
		output.setPosition(0);
		output.writeInt(total - 4);
		return ChannelBuffers.wrappedBuffer(output.getBuffer(), 0, total);
	}
}
