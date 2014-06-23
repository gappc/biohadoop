package at.ac.uibk.dps.biohadoop.queue;

import static org.junit.Assert.*;

import org.junit.Test;

public class TaskFutureImplTest {

	@Test
	public void isDoneToEarly() {
		TaskFutureImpl<Integer> taskFutureImpl = new TaskFutureImpl<>();
		assertEquals(false, taskFutureImpl.isDone());
	}
	
	@Test
	public void isDoneAfterSet() {
		TaskFutureImpl<Integer> taskFutureImpl = new TaskFutureImpl<>();
		taskFutureImpl.set(0);
		assertEquals(true, taskFutureImpl.isDone());
	}

}
