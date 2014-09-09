package at.ac.uibk.dps.biohadoop.queue;

import org.junit.Test;

import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskCompletionService;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskException;

public class TaskCompletionServiceTest {

	@Test(expected = NullPointerException.class)
	public void awaitAllParamIsNull() throws TaskException {
		TaskCompletionService.awaitAll(null);
	}

}
