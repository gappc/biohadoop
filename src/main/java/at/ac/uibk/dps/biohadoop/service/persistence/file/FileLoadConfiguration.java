package at.ac.uibk.dps.biohadoop.service.persistence.file;

import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileLoadConfiguration implements HandlerConfiguration {

	private final Class<? extends Handler> handler;
	private final String path;
	private final boolean onStartup;

	public FileLoadConfiguration(Class<? extends Handler> handler, String path,
			boolean onStartup) {
		this.handler = handler;
		this.path = path;
		this.onStartup = onStartup;
	}

	@JsonCreator
	public static FileLoadConfiguration create(
			@JsonProperty("handler") Class<? extends Handler> handler,
			@JsonProperty("path") String path,
			@JsonProperty("onStartup") boolean onStartup) {
		return new FileLoadConfiguration(handler, path, onStartup);
	}

	@Override
	public Class<? extends Handler> getHandler() {
		return handler;
	}

	public String getPath() {
		return path;
	}

	public boolean isOnStartup() {
		return onStartup;
	}

}
