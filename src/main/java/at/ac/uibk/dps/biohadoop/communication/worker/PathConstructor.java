package at.ac.uibk.dps.biohadoop.communication.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.uibk.dps.biohadoop.communication.RemoteExecutable;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedLocal;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.queue.SimpleTaskSubmitter;

//TODO refactor
public class PathConstructor {

	private static final Logger LOG = LoggerFactory
			.getLogger(PathConstructor.class);

	public static String getRestPath(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws WorkerException {
		String path = SimpleTaskSubmitter.QUEUE_NAME;
		if (remoteExecutable != null) {
			DedicatedRest dedicated = remoteExecutable
					.getAnnotation(DedicatedRest.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}

	public static String getWebSocketPath(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws WorkerException {
		String path = SimpleTaskSubmitter.QUEUE_NAME;
		if (remoteExecutable != null) {
			DedicatedWebSocket dedicated = remoteExecutable
					.getAnnotation(DedicatedWebSocket.class);
			if (dedicated != null) {
				path = dedicated.queueName();
				LOG.info("Adding dedicated Rest resource at path {}", path);
			} else {
				LOG.error("No suitable annotation for Rest resource found");
			}
		}
		return path;
	}

	public static String getLocalPath(
			Class<? extends RemoteExecutable<?, ?, ?>> remoteExecutable)
			throws WorkerException {
		String path = SimpleTaskSubmitter.QUEUE_NAME;
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
