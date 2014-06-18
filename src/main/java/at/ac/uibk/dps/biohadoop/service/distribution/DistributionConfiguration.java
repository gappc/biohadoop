package at.ac.uibk.dps.biohadoop.service.distribution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributionConfiguration {

	private final Class<? extends DataMerger> dataMerger;
	private final int mergeAfterIterations;

	public DistributionConfiguration(Class<? extends DataMerger> dataMerger,
			int mergeAfterIterations) {
		this.dataMerger = dataMerger;
		this.mergeAfterIterations = mergeAfterIterations;
	}

	@JsonCreator
	public static DistributionConfiguration create(
			@JsonProperty("dataMerger") Class<? extends DataMerger> dataMerger,
			@JsonProperty("mergeAfterIterations") int mergeAfterIterations) {
		return new DistributionConfiguration(dataMerger, mergeAfterIterations);
	}

	public Class<? extends DataMerger> getDataMerger() {
		return dataMerger;
	}

	public int getMergeAfterIterations() {
		return mergeAfterIterations;
	}
}
