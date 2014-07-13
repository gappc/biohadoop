package at.ac.uibk.dps.biohadoop.queue;

import java.util.List;

public class TaskCompletionService {

	private TaskCompletionService() {
	}

	public static <T> void awaitAll(List<TaskFuture<T>> taskFutures)
			throws InterruptedException {
		for (TaskFuture<T> taskFuture : taskFutures) {
			taskFuture.get();
		}
	}
}
