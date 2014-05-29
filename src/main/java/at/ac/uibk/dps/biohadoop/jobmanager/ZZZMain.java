package at.ac.uibk.dps.biohadoop.jobmanager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import at.ac.uibk.dps.biohadoop.jobmanager.api.JobManager;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobRequest;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobRequestData;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponse;
import at.ac.uibk.dps.biohadoop.jobmanager.api.JobResponseData;
import at.ac.uibk.dps.biohadoop.jobmanager.handler.JobHandler;

public class ZZZMain {

	private final int JOB_COUNT = 100;
	private final int THREAD_COUNT = 2;
	private static AtomicInteger shutdowns = new AtomicInteger();

	public static void main(String[] args) throws InterruptedException {
		ZZZMain main = new ZZZMain();
		main.run();
		new CountDownLatch(1).await(3, TimeUnit.SECONDS);
	}

	private void run() throws InterruptedException {
		JobRequest<Integer> jobRequest = new JobRequest<>(
				new PrivateJobHandler());
		for (int i = 0; i < JOB_COUNT; i++) {
			jobRequest.add(new JobRequestData<Integer>(i, i));
		}

		final JobManager<Integer> jobManager = JobManager.getInstance();

		for (int i = 0; i < THREAD_COUNT; i++) {
			new Thread(new Consumer(jobManager, i), "Consumer-" + i).start();
		}

		JobId jobId = jobManager.submitJob(jobRequest,
				Integer.class.getCanonicalName());

		// System.out.println(jobManager.getState(jobId,
		// Integer.class.getCanonicalName()));
		// for (int i = 0; i < THREAD_COUNT; i++) {
		// new Thread(new Consumer(jobManager, i)).start();
		// }

		System.out.println("SLEEPING");
		// System.out.println(jobId + " " + jobManager.getState(jobId,
		// Integer.class.getCanonicalName()));
		Thread.sleep(100);
		// System.out.println(jobManager.getState(jobId,
		// Integer.class.getCanonicalName()));
		System.out.println("WOKE UP");
		jobId = jobManager.submitJob(jobRequest,
				Integer.class.getCanonicalName());
		// System.out.println(jobId + " " + jobManager.getState(jobId,
		// Integer.class.getCanonicalName()));

		Thread.sleep(1000);
		System.out.println("NOW KILLING");
		jobManager.shutdown();
		Thread.sleep(1000);
		System.out.println(shutdowns);
	}

	private class PrivateJobHandler implements JobHandler<Integer> {

		@Override
		public void onNew() {
			// System.out.println("onNew");
		}

		@Override
		public void onSubmitted() {
			// System.out.println("onSubmitted");
		}

		@Override
		public void onRunning() {
			// System.out.println("onRunning");
		}

		@Override
		public void onFinished(JobResponse<Integer> jobResponse) {
			// System.out.println("----------onFinished---------");
			for (int i = 0; i < jobResponse.getResponseData().size(); i++) {
				JobResponseData<Integer> data = jobResponse.getResponseData()
						.get(i);
				// System.out.println(i + " " + data);
			}
			System.out.println("-----" + jobResponse.getJobId()
					+ "-----onFinished with "
					+ jobResponse.getResponseData().size()
					+ " elements ---------");
		}

		@Override
		public void onError() {
			System.out.println("onError");
		}

	}

	private class Consumer implements Runnable {

		private final JobManager<Integer> jobManager = JobManager.getInstance();
		private int i;

		public Consumer(JobManager jobManager, int i) {
			this.i = i;
		}

		@Override
		public void run() {
			int count = 0;
			for (;;) {
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Task<Integer> data = jobManager.getTask(Integer.class
						.getCanonicalName());
				if (data == null) {
					shutdowns.incrementAndGet();
					break;
				}
				System.out.println(i + " " + data.getData() + " | count: "
						+ ++count);
				// System.out.println(data.getData());
				jobManager.putResult(data, Integer.class.getCanonicalName());
			}
		}

	}

}
