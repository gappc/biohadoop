package at.ac.uibk.dps.biohadoop.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedLocal;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;
import at.ac.uibk.dps.biohadoop.unifiedcommunication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.utils.ResourcePath;

//TODO refactor
public class WorkerInitializer {

	private static final Logger LOG = LoggerFactory
			.getLogger(WorkerInitializer.class);

	public static String getRestPath(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws WorkerException {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (remoteExecutable != null) {
			DedicatedRest dedicated = remoteExecutable
					.getAnnotation(DedicatedRest.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
				ResourcePath.addRestEntry(path, remoteExecutable);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}

	public static String getWebSocketPath(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws WorkerException {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (remoteExecutable != null) {
			DedicatedWebSocket dedicated = remoteExecutable
					.getAnnotation(DedicatedWebSocket.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
				ResourcePath.addWebSocketEntry(path, remoteExecutable);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}

	public static String getLocalPath(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws WorkerException {
		String path = DefaultTaskClient.QUEUE_NAME;
		if (remoteExecutable != null) {
			DedicatedLocal dedicated = remoteExecutable
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
