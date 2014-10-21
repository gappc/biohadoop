package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class WorkerConfiguration {

	private final Class<? extends Worker> worker;
	private final String pipelineName;
	private final Integer count;

	public WorkerConfiguration(Class<? extends Worker> worker,
			String pipelineName,
			Integer count) {
		this.worker = worker;
		this.pipelineName = pipelineName;
		this.count = count;
	}

	@JsonCreator
	public static WorkerConfiguration create(
			@JsonProperty("worker") Class<? extends Worker> worker,
			@JsonProperty("pipelineName") String pipelineName,
			@JsonProperty("count") Integer count) {
		return new WorkerConfiguration(worker, pipelineName, count);
	}

	public Class<? extends Worker> getWorker() {
		return worker;
	}

	public String getPipelineName() {
		return pipelineName;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public String toString() {
		String workerClassname = worker != null ? worker.getCanonicalName() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("worker=").append(workerClassname);
		sb.append(" pipelineName=").append(pipelineName);
		sb.append(" count=").append(count);
		return sb.toString();
	}

}
