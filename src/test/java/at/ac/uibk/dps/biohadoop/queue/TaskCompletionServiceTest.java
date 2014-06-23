package at.ac.uibk.dps.biohadoop.queue;

import org.junit.Test;

public class TaskCompletionServiceTest {

	@Test(expected = NullPointerException.class)
	public void awaitAllParamIsNull() throws InterruptedException {
		TaskCompletionService.awaitAll(null);
	}

}
