package at.ac.uibk.dps.biohadoop.service.distribution;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DistributionConfiguration {

	private final Class<? extends DataMerger> dataMerger;

	public DistributionConfiguration(Class<? extends DataMerger> dataMerger) {
		this.dataMerger = dataMerger;
	}
	
	@JsonCreator
	public static DistributionConfiguration create(
			@JsonProperty("dataMerger") Class<? extends DataMerger> dataMerger) {
		return new DistributionConfiguration(dataMerger);
	}

	public Class<? extends DataMerger> getDataMerger() {
		return dataMerger;
	}
	
}
