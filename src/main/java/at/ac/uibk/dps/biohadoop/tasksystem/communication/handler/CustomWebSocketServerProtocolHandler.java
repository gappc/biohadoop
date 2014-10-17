package at.ac.uibk.dps.biohadoop.tasksystem.communication.handler;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWebSocketServerProtocolHandler extends WebSocketServerProtocolHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(CustomWebSocketServerProtocolHandler.class);
	
	public CustomWebSocketServerProtocolHandler(String websocketPath) {
		super(websocketPath);
	}
	
	public CustomWebSocketServerProtocolHandler(String websocketPath, String subprotocols) {
		super(websocketPath, subprotocols);
	}

	public CustomWebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions) {
		super(websocketPath, subprotocols, allowExtensions);
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof WebSocketHandshakeException) {
            DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            response.setContent(ChannelBuffers.wrappedBuffer(e.getCause().getMessage().getBytes()));
            ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
        	LOG.error("Handler error: ", e.getCause());
            ctx.getChannel().close();
        }
    }
    
}
