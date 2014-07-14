package at.ac.uibk.dps.biohadoop.handler.persistence.file;

import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileSaveConfiguration implements HandlerConfiguration {

	private final Class<? extends Handler> handler;
	private final String path;
	private final int afterIterations;

	public FileSaveConfiguration(Class<? extends Handler> handler, String path, int afterIterations) {
		this.handler = handler;
		this.path = path;
		this.afterIterations = afterIterations;
	}
	
	@JsonCreator
	public static FileSaveConfiguration create(
			@JsonProperty("handler") Class<? extends Handler> handler,
			@JsonProperty("path") String path,
			@JsonProperty("afterIterations") int afterIterations) {
		return new FileSaveConfiguration(handler, path, afterIterations);
	}
	
	@Override
	public Class<? extends Handler> getHandler() {
		return handler;
	}

	public String getPath() {
		return path;
	}
	
	public int getAfterIterations() {
		return afterIterations;
	}

}
