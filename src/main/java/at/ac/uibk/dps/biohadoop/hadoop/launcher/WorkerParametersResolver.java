package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;

import at.ac.uibk.dps.biohadoop.communication.WorkerConfiguration;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedKryo;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedLocal;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedSocket;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;
import at.ac.uibk.dps.biohadoop.queue.DefaultTaskClient;

public class WorkerParametersResolver {

	public static String getKryoWorkerParameters(
			WorkerConfiguration workerConfiguration) throws Exception {
		Annotation workerAnnotation = workerConfiguration.getRemoteExecutable()
				.getAnnotation(DedicatedKryo.class);
		String prefix = DefaultTaskClient.QUEUE_NAME;
		if (workerAnnotation != null && workerConfiguration.getAnnotation() != null) {
			prefix = ((DedicatedKryo) workerAnnotation).queueName();
		}
		String hostname = Environment.getPrefixed(prefix,
				Environment.KRYO_SOCKET_HOST);
		String port = Environment.getPrefixed(prefix,
				Environment.KRYO_SOCKET_PORT);
		return workerConfiguration.getWorker().getCanonicalName() + " " + hostname + " " + port;
	}

	public static String getLocalWorkerParameters(
			WorkerConfiguration workerConfiguration) throws Exception {
		Annotation workerAnnotation = workerConfiguration.getRemoteExecutable()
				.getAnnotation(DedicatedLocal.class);
		if (workerAnnotation != null) {
			return "EMPTY";
		}
		return null;
	}

	public static String getRestWorkerParameters(
			WorkerConfiguration workerConfiguration) throws Exception {
		String hostname = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);
		return workerConfiguration.getWorker().getCanonicalName() + " " + hostname + " " + port;
	}

	public static String getSocketWorkerParameters(
			WorkerConfiguration workerConfiguration) throws Exception {
		Annotation workerAnnotation = workerConfiguration.getRemoteExecutable()
				.getAnnotation(DedicatedSocket.class);
		String prefix = DefaultTaskClient.QUEUE_NAME;
		if (workerAnnotation != null && workerConfiguration.getAnnotation() != null) {
			prefix = ((DedicatedSocket) workerAnnotation).queueName();
		}
		String hostname = Environment.getPrefixed(prefix,
				Environment.SOCKET_HOST);
		String port = Environment.getPrefixed(prefix,
				Environment.SOCKET_PORT);
		return workerConfiguration.getWorker().getCanonicalName() + " " + hostname + " " + port;
	}

	public static String getWebSocketWorkerParameters(
			WorkerConfiguration workerConfiguration) throws Exception {
		String hostname = Environment.get(Environment.HTTP_HOST);
		String port = Environment.get(Environment.HTTP_PORT);
		return workerConfiguration.getWorker().getCanonicalName() + " " + hostname + " " + port;
	}
}
