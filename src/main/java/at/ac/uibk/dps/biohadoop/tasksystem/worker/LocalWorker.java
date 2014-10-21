package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.AsyncComputable;
import at.ac.uibk.dps.biohadoop.tasksystem.ComputeException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.Task;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskConfiguration;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueueService;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskTypeId;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class LocalWorker<R, T, S> implements Worker, Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalWorker.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(LocalWorker.class);

	private final Map<TaskTypeId, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
	private final AtomicBoolean stop = new AtomicBoolean(false);

	private String pipelineName;
	private int logSteps = 1000;

	// TODO check for correct implementation
//	@Override
//	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
//			throws WorkerLaunchException {
//		return null;
//	}

//	@Override
//	public void configure(String[] args) throws WorkerException {
//		pipelineName = args[0];
//	}
//
	@Override
	public void start(String host, int port) throws WorkerException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object call() throws WorkerException {
		LOG.info("############# {} started for pipeline {} ##############",
				CLASSNAME, pipelineName);

		TaskQueue<R, T, S> taskQueue = TaskQueueService
				.getTaskQueue(pipelineName);

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (!stop.get()) {
			try {
				// performanceLogger.step(LOG);

				Task<T> task = taskQueue.getTask();
				if (task == null) {
					LOG.info("############# {} Worker stopped ###############",
							CLASSNAME);
					break;
				}

				TaskTypeId taskTypeId = task.getTaskTypeId();
				WorkerData<R, T, S> workerData = workerDatas.get(taskTypeId);
				if (workerData == null) {
					workerData = getInitialData(taskQueue, task);
					workerDatas.put(taskTypeId, workerData);
				}

				AsyncComputable<R, T, S> asyncComputable = workerData
						.getAsyncComputable();
				R initialData = workerData.getInitialData();

				T data = task.getData();
				S result = asyncComputable.compute(data, initialData);

				taskQueue.storeResult(task.getTaskId(), result);
			} catch (InterruptedException e) {
				// InterruptedException means to shutdown
				throw new WorkerException(
						"Got ShutdownException, stopping work", e);
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException | TaskException e) {
				throw new WorkerException(
						"Error while execution, stopping work", e);
			} catch (ComputeException e) {
				throw new WorkerException(
						"Error while computing result, stopping work", e);
			}
		}
		return null;
	}

	public void stop() {
		stop.set(true);
	}

	private WorkerData<R, T, S> getInitialData(TaskQueue<R, T, S> taskQueue,
			Task<T> task) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, TaskException {
		TaskConfiguration<R> taskConfiguration = taskQueue
				.getTaskConfiguration(task.getTaskId());

		String asyncComputableClassName = taskConfiguration
				.getAsyncComputableClassName();
		Class<? extends AsyncComputable<R, T, S>> asyncComputableClass = (Class<? extends AsyncComputable<R, T, S>>) Class
				.forName(asyncComputableClassName);
		AsyncComputable<R, T, S> asyncComputable = asyncComputableClass
				.newInstance();

		return new WorkerData<>(asyncComputable,
				taskConfiguration.getInitialData());
	}

}
