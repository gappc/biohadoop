package at.ac.uibk.dps.biohadoop.communication;

import java.lang.annotation.Annotation;

import at.ac.uibk.dps.biohadoop.communication.master.MasterEndpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MasterConfiguration {

	private final Class<? extends MasterEndpoint> master;
	private final Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable;
	private final Class<? extends Annotation> annotation;

	public MasterConfiguration(Class<? extends MasterEndpoint> master,
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			Class<? extends Annotation> annotation) {
		this.master = master;
		this.remoteExecutable = remoteExecutable;
		this.annotation = annotation;
	}

	@JsonCreator
	public static MasterConfiguration create(
			@JsonProperty("master") Class<? extends MasterEndpoint> master,
			@JsonProperty("remoteExecutable") Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable,
			@JsonProperty("annotation") Class<? extends Annotation> annotation) {
		return new MasterConfiguration(master, remoteExecutable, annotation);
	}

	public Class<? extends MasterEndpoint> getMaster() {
		return master;
	}

	public Class<? extends RemoteExecutable<?, ?, ?>> getRemoteExecutable() {
		return remoteExecutable;
	}

	public Class<? extends Annotation> getAnnotation() {
		return annotation;
	}

	@Override
	public String toString() {
		String masterClass = master != null ? master.getCanonicalName() : null;
		String remoteExecutableClass = remoteExecutable != null ? remoteExecutable
				.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("MasterEndpoint=").append(masterClass);
		sb.append(" RemoteExecutable=").append(remoteExecutableClass);
		sb.append(" Annotation=").append(annotation);
		return sb.toString();
	}

}
