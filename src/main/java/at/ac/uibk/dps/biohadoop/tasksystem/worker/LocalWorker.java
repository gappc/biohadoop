package at.ac.uibk.dps.biohadoop.tasksystem.worker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.tasksystem.ComputeException;
import at.ac.uibk.dps.biohadoop.tasksystem.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskException;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueue;
import at.ac.uibk.dps.biohadoop.tasksystem.queue.TaskQueueService;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class LocalWorker<R, T, S> implements Worker,
		Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalWorker.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(LocalWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
	private final AtomicBoolean stop = new AtomicBoolean(false);

	private String settingName;
	private int logSteps = 1000;

	// TODO check for correct implementation
	@Override
	public String buildLaunchArguments(WorkerConfiguration workerConfiguration)
			throws WorkerLaunchException {
		return null;
	}

	@Override
	public void configure(String[] args) throws WorkerException {
		settingName = args[0];
	}

	@Override
	public void start() throws WorkerException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object call() throws WorkerException {
		LOG.info("############# {} started for setting {} ##############",
				CLASSNAME, settingName);

		TaskQueue<R, T, S> taskQueue = TaskQueueService.getInstance()
				.getTaskQueue(settingName);

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (!stop.get()) {
			try {
				// performanceLogger.step(LOG);

				ClassNameWrappedTask<T> task = (ClassNameWrappedTask<T>) taskQueue
						.getTask();
				if (task == null) {
					LOG.info("############# {} Worker stopped ###############",
							CLASSNAME);
					break;
				}

				String className = task.getClassName();
				WorkerData<R, T, S> workerData = workerDatas.get(className);
				if (workerData == null) {
					workerData = getInitialData(taskQueue, task);
					workerDatas.put(className, workerData);
				}

				RemoteExecutable<R, T, S> remoteExecutable = workerData
						.getRemoteExecutable();
				R initialData = workerData.getInitialData();

				T data = task.getData();
				S result = remoteExecutable.compute(data, initialData);

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
			ClassNameWrappedTask<T> task) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, TaskException {
		String className = task.getClassName();
		Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass = (Class<? extends RemoteExecutable<R, T, S>>) Class
				.forName(className);
		RemoteExecutable<R, T, S> remoteExecutable = remoteExecutableClass
				.newInstance();

		R initialData = taskQueue.getInitialData(task.getTaskId());
		return new WorkerData<>(remoteExecutable, initialData);
	}

}
