package at.ac.uibk.dps.biohadoop.queue;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskBrokerService;

public class TaskQueueServiceTest {

	@Test
	public void taskBrokerNotNull() {
		assertNotNull(TaskBrokerService.getTaskBroker());
	}

}
