package at.ac.uibk.dps.biohadoop.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TaskQueueServiceTest {

	@Test
	public void taskQueueNotNull() {
		assertNotNull(TaskQueueService.getInstance().getTaskQueue(""));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void taskQueueSameQueuenameIsSameQueue() {
		TaskQueue<Integer, Integer> taskQueue1 = (TaskQueue<Integer, Integer>) TaskQueueService
				.getInstance().getTaskQueue("0");
		TaskQueue<Integer, Integer> taskQueue2 = (TaskQueue<Integer, Integer>) TaskQueueService
				.getInstance().getTaskQueue("0");
		assertEquals(taskQueue1, taskQueue2);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void taskQueueDifferentQueuenameIsDifferentQueues() {
		TaskQueue<Integer, Integer> taskQueue1 = (TaskQueue<Integer, Integer>) TaskQueueService
				.getInstance().getTaskQueue("0");
		TaskQueue<Integer, Integer> taskQueue2 = (TaskQueue<Integer, Integer>) TaskQueueService
				.getInstance().getTaskQueue("1");
		assertNotEquals(taskQueue1, taskQueue2);
	}
}