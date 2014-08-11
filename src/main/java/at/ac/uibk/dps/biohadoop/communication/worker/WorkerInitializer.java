package at.ac.uibk.dps.biohadoop.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.master.DedicatedLocal;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.master.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ResourcePath;

//TODO refactor
public class WorkerInitializer {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerInitializer.class);

	public static String getRestPath(String className) throws WorkerException {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (className != null && className.length() > 0) {
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass;
			try {
				remoteExecutableClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
						.forName(className);
			} catch (ClassNotFoundException e) {
				throw new WorkerException("Could not find class " + className,
						e);
			}
			DedicatedRest dedicated = remoteExecutableClass
					.getAnnotation(DedicatedRest.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
				ResourcePath.addRestEntry(path, remoteExecutableClass);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}

	public static String getWebSocketPath(String className)
			throws WorkerException {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (className != null && className.length() > 0) {
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass;
			try {
				remoteExecutableClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
						.forName(className);
			} catch (ClassNotFoundException e) {
				throw new WorkerException("Could not find class " + className,
						e);
			}
			DedicatedWebSocket dedicated = remoteExecutableClass
					.getAnnotation(DedicatedWebSocket.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
				ResourcePath.addWebSocketEntry(path, remoteExecutableClass);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}

	public static String getLocalPath(String className) throws WorkerException {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (className != null && className.length() > 0) {
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutableClass;
			try {
				remoteExecutableClass = (Class<? extends RemoteExecutable<?, ?, ?>>) Class
						.forName(className);
			} catch (ClassNotFoundException e) {
				throw new WorkerException("Could not find class " + className,
						e);
			}
			DedicatedLocal dedicated = remoteExecutableClass
					.getAnnotation(DedicatedLocal.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}
}
