package at.ac.uibk.dps.biohadoop.islandmodel.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.algorithm.AlgorithmId;
import at.ac.uibk.dps.biohadoop.tasksystem.communication.mapper.JsonMapper;

public class IslandModelDataHandler extends SimpleChannelUpstreamHandler {

	private static final Logger LOG = LoggerFactory
			.getLogger(IslandModelDataHandler.class);
	public static final String PATH = "islandmodel";
	
	private static final Map<AlgorithmId, Object> DATA_FOR_ALLGORITHM = new ConcurrentHashMap<>();

	public static void publish(AlgorithmId algorithmId, Object data) {
		DATA_FOR_ALLGORITHM.put(algorithmId, data);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		DefaultHttpRequest request = (DefaultHttpRequest) e.getMessage();
		DefaultHttpResponse response = new DefaultHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
		response.setHeader("Content-Type", "application/json");
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

		String[] pathParams = request.getUri().split("/");
		String id = pathParams[pathParams.length - 1];
		if (id == null || id.length() == 0) {
			LOG.error("Could not parse algorithm id for path {}",
					request.getUri());
			String output = "Invalid path: " + request.getUri();
			buffer.writeBytes(output.getBytes());
			response.setContent(buffer);
			response.setStatus(HttpResponseStatus.BAD_REQUEST);
			response.setHeader("Content-Length", output.length());
		} else {
			LOG.debug("Sending data for algorithmId {}", id);
			AlgorithmId algorithmId = AlgorithmId.valueOf(id);
			Object data = DATA_FOR_ALLGORITHM.get(algorithmId);
			String output = JsonMapper.OBJECT_MAPPER.writeValueAsString(data);
			buffer.writeBytes(output.getBytes());
			response.setContent(buffer);
			response.setStatus(HttpResponseStatus.OK);
			response.setHeader("Content-Length", output.length());
		}
		e.getChannel().write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		LOG.error("Error while responding with island data");
		super.exceptionCaught(ctx, e);
	}

}
