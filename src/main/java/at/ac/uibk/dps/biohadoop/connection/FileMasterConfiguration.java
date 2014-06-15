package at.ac.uibk.dps.biohadoop.connection;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileMasterConfiguration {

	private final List<Class<? extends MasterConnection>> endpoints;

	public FileMasterConfiguration(List<Class<? extends MasterConnection>> endpoints) {
		this.endpoints = endpoints;
	}

	@JsonCreator
	public static FileMasterConfiguration create(
			@JsonProperty("endpoints") List<Class<? extends MasterConnection>> endpoints) {
		return new FileMasterConfiguration(endpoints);
	}

	public List<Class<? extends MasterConnection>> getEndpoints() {
		return endpoints;
	}

//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		if (endpoints != null) {
//			for (Class<?> endpoint : endpoints) {
//				sb.append(" ").append(endpoint.getCanonicalName());
//			}
//		}
//		else {
//			sb.append(" null");
//		}
//		return sb.toString();
//	}
}
