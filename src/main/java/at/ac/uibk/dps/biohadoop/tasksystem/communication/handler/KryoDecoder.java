package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.CompositeChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.UnsafeInput;

/**
 * Taken from https://github.com/EsotericSoftware/kryonetty/blob/master/src/com/esotericsoftware/kryonetty/KryoChannelPipelineFactory.java
 * 
 * @author Christian Gapp
 *
 */
public class KryoDecoder extends FrameDecoder {
	private final Kryo kryo;
	private final Input input = new UnsafeInput();
	private int length = -1;

	public KryoDecoder(Kryo kryo) {
		this.kryo = kryo;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		byte[] data = null;
		if (buffer instanceof CompositeChannelBuffer) {
			data = buffer.toByteBuffer().array();
//			System.out.println(buffer.toByteBuffer().array() toString());
		}
		else {
			data = buffer.array();
		}
		input.setBuffer(data, buffer.readerIndex(),
				buffer.readableBytes());
		if (length == -1) {
			// Read length.
			if (buffer.readableBytes() < 4)
				return null;
			length = input.readInt();
			buffer.readerIndex(input.position());
		}
		if (buffer.readableBytes() < length)
			return null;
		length = -1;
		Object object = kryo.readClassAndObject(input);
		// Dumps out bytes for JMeter's TCP Sampler (BinaryTCPClientImpl
		// classname):
		// System.out.println("--");
		// for (int i = buffer.readerIndex() - 4; i < input.position(); i++) {
		// String hex = Integer.toHexString(input.getBuffer()[i] & 0xff);
		// if (hex.length() == 1) hex = "0" + hex;
		// System.out.print(hex.toUpperCase());
		// }
		// System.out.println("\n--");
		buffer.readerIndex(input.position());
		return object;
	}
}
