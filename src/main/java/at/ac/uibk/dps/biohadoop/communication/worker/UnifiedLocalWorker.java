package at.ac.uibk.dps.biohadoop.communication.worker;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.hadoop.launcher.WorkerLaunchException;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpoint;
import at.ac.uibk.dps.biohadoop.queue.TaskEndpointImpl;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.ClassNameWrappedTask;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.WorkerData;
import at.ac.uibk.dps.biohadoop.utils.ClassnameProvider;
import at.ac.uibk.dps.biohadoop.utils.PerformanceLogger;

public class UnifiedLocalWorker<R, T, S> implements WorkerEndpoint,
		Callable<Integer> {

	private static final Logger LOG = LoggerFactory
			.getLogger(UnifiedLocalWorker.class);
	private static final String CLASSNAME = ClassnameProvider
			.getClassname(UnifiedLocalWorker.class);

	private final Map<String, WorkerData<R, T, S>> workerDatas = new ConcurrentHashMap<>();
	private final AtomicBoolean stop = new AtomicBoolean(false);

	private String path;
	private int logSteps = 1000;

	@Override
	public void configure(String[] args) throws WorkerException {
		WorkerParameters parameters = WorkerParameters.getParameters(args);
		path = WorkerInitializer.getLocalPath(parameters.getRemoteExecutable());
	}

	@Override
	public void start() throws WorkerException {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer call() {
		LOG.info("############# {} started ##############", CLASSNAME);

		TaskEndpoint<T, S> taskEndpoint = new TaskEndpointImpl<>(path);
		boolean registrationInit = false;

		PerformanceLogger performanceLogger = new PerformanceLogger(
				System.currentTimeMillis(), 0, logSteps);
		while (!stop.get()) {
			try {
				performanceLogger.step(LOG);

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
					workerData = getInitialData(className);
					workerDatas.put(className, workerData);
				}

				// if (!registrationInit) {
				// doRegistrationInit();
				// registrationInit = true;
				// }
				RemoteExecutable<R, T, S> remoteExecutable = workerData
						.getRemoteExecutable();
				R initialData = workerData.getInitialData();

				T data = task.getData();
				S result = remoteExecutable.compute(data, initialData);

				taskEndpoint.storeResult(task.getTaskId(), result);
			} catch (InterruptedException e) {
				LOG.debug("Got InterruptedException, stopping work");
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	public void stop() {
		stop.set(true);
	}

	public void run(String host, int port) throws WorkerException {
		LOG.error("run");
	}

	private WorkerData<R, T, S> getInitialData(String className)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		Class<? extends RemoteExecutable<R, T, S>> remoteExecutableClass = (Class<? extends RemoteExecutable<R, T, S>>) Class
				.forName(className);
		RemoteExecutable<R, T, S> remoteExecutable = remoteExecutableClass
				.newInstance();

		return new WorkerData<>(remoteExecutable,
				remoteExecutable.getInitalData());
	}

	// TODO check for correct implementation
	@Override
	public String getWorkerParameters(WorkerConfiguration workerConfiguration) throws WorkerLaunchException {
		return null;
	}

}
