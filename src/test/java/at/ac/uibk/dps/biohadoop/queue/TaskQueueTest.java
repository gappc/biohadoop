package at.ac.uibk.dps.biohadoop.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TaskQueueTest {

	@Test
	public void addPararmIsNull() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		TaskFuture<Integer> taskFuture = taskQueue.add(null);
		assertNotNull(taskFuture);
	}
	
	@Test
	public void addTaskRequest() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		TaskFuture<Integer> taskFuture = taskQueue.add(0);
		assertNotNull(taskFuture);
	}

	@Test(expected = NullPointerException.class)
	public void addAllPararmIsNull() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.addAll(null);
	}

	@Test
	public void addAllEmptyList() throws InterruptedException {
		List<Integer> taskRequestList = new ArrayList<>();
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		List<TaskFuture<Integer>> taskFutures = taskQueue.addAll(taskRequestList);
		assertNotNull(taskFutures);
		assertEquals(0, taskFutures.size());
	}
	
	@Test
	public void addAllSomeElementsInList() throws InterruptedException {
		int size = 3;
		List<Integer> taskRequestList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			taskRequestList.add(0);
		}

		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		List<TaskFuture<Integer>> taskFutures = taskQueue.addAll(taskRequestList);
		
		assertEquals(size, taskFutures.size());
	}
	
	@Test
	public void getTaskNotNull() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.add(0);
		Task<Integer> task = taskQueue.getTask();
		assertNotNull(task);
	}
	
	@Test
	public void getTaskValueCorrect() throws InterruptedException {
		int value = 234501;
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.add(value);
		Task<Integer> task = taskQueue.getTask();
		int taskData = task.getData();
		assertEquals(value, taskData);
	}
	
	@Test(expected = NullPointerException.class)
	public void putResultTaskIdNull() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.putResult(null, null);
	}
	
	@Test(expected = NullPointerException.class)
	public void putResultTaskIdWrong() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.putResult(TaskId.newInstance(), null);
	}
	
	@Test
	public void putResultCorrectValue() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		TaskFuture<Integer> taskFuture = taskQueue.add(0);
		
		int result = 234501;
		Task<Integer> in = taskQueue.getTask();
		taskQueue.putResult(in.getTaskId(), result);

		int taskResult = taskFuture.get();
		assertEquals(result, taskResult);
	}
	
	@Test(expected = NullPointerException.class)
	public void rescheduleParamIsNull() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.reschedule(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void rescheduleTaskIdIsWrong() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.reschedule(TaskId.newInstance());
	}
	
	@Test
	public void rescheduleTaskIdIsRight() throws InterruptedException {
		TaskQueue<Integer, Integer> taskQueue = new TaskQueue<>();
		taskQueue.add(0);
		
		Task<Integer> in = taskQueue.getTask();
		
		taskQueue.reschedule(in.getTaskId());
		assertTrue(true);
	}
}
