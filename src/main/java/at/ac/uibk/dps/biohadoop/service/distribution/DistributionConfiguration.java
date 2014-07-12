package at.ac.uibk.dps.biohadoop.service.distribution;

import at.ac.uibk.dps.biohadoop.handler.Handler;
import at.ac.uibk.dps.biohadoop.handler.HandlerConfiguration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributionConfiguration implements HandlerConfiguration {

	private final Class<? extends Handler> handler;
	private final Class<? extends DataMerger<?>> dataMerger;
	private final Class<? extends RemoteResultGetter> remoteResultGetter;
	private final int mergeAfterIterations;

	public DistributionConfiguration(Class<? extends Handler> handler,
			Class<? extends DataMerger<?>> dataMerger,
			Class<? extends RemoteResultGetter> remoteResultGetter,
			int mergeAfterIterations) {
		this.handler = handler;
		this.dataMerger = dataMerger;
		this.remoteResultGetter = remoteResultGetter;
		this.mergeAfterIterations = mergeAfterIterations;
	}

	@JsonCreator
	public static DistributionConfiguration create(
			@JsonProperty("handler") Class<? extends Handler> handler,
			@JsonProperty("dataMerger") Class<? extends DataMerger<?>> dataMerger,
			@JsonProperty("remoteResultGetter") Class<? extends RemoteResultGetter> remoteResultGetter,
			@JsonProperty("mergeAfterIterations") int mergeAfterIterations) {
		return new DistributionConfiguration(handler, dataMerger,
				remoteResultGetter, mergeAfterIterations);
	}

	@Override
	public Class<? extends Handler> getHandler() {
		return handler;
	}

	public Class<? extends DataMerger<?>> getDataMerger() {
		return dataMerger;
	}
	
	public Class<? extends RemoteResultGetter> getRemoteResultGetter() {
		return remoteResultGetter;
	}

	public int getMergeAfterIterations() {
		return mergeAfterIterations;
	}

}
