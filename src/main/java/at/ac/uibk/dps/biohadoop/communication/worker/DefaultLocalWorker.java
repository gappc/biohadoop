package at.ac.uibk.dps.biohadoop.communication.worker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.communication.ComputeException;
import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.ShutdownException;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.queue.TaskException;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class DefaultLocalWorker<R, T, S> implements WorkerEndpoint,
		Callable<Object> {

	private static final Logger LOG = LoggerFactory
			.getLogger(DefaultLocalWorker.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(DefaultLocalWorker.class);

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

		TaskEndpoint<R, T, S> taskEndpoint = new TaskEndpointImpl<>(settingName);

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (!stop.get()) {
			try {
				// performanceLogger.step(LOG);

				ClassNameWrappedTask<T> task = (ClassNameWrappedTask<T>) taskEndpoint
						.getTask();
				if (task == null) {
					LOG.info("############# {} Worker stopped ###############",
							CLASSNAME);
					break;
				}

				String className = task.getClassName();
				WorkerData<R, T, S> workerData = workerDatas.get(className);
				if (workerData == null) {
					workerData = getInitialData(taskEndpoint, task);
					workerDatas.put(className, workerData);
				}

				RemoteExecutable<R, T, S> remoteExecutable = workerData
						.getRemoteExecutable();
				R initialData = workerData.getInitialData();

				T data = task.getData();
				S result = remoteExecutable.compute(data, initialData);

				taskEndpoint.storeResult(task.getTaskId(), result);
			} catch (ShutdownException e) {
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

	private WorkerData<R, T, S> getInitialData(TaskEndpoint<R, T, S> taskEndpoint, ClassNameWrappedTask<T> task)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, TaskException {
		String className = task.getClassName();
		Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass = (Class<? extends RemoteExecutable<R, T, S>>) Class
				.forName(className);
		RemoteExecutable<R, T, S> remoteExecutable = remoteExecutableClass
				.newInstance();

		R initialData = taskEndpoint.getInitialData(task.getTaskId());
		return new WorkerData<>(remoteExecutable, initialData);
	}

}
