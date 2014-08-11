package at.ac.uibk.dps.biohadoop.hadoop.launcher;

import java.lang.annotation.Annotation;

import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedKryo;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedLocal;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedRest;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedSocket;
import at.ac.uibk.dps.biohadoop.communication.annotation.DedicatedWebSocket;
import at.ac.uibk.dps.biohadoop.deletable.Worker;
import at.ac.uibk.dps.biohadoop.hadoop.Environment;

public class WorkerParametersResolver {

	public static String getKryoWorkerParameters(
			Class<? extends Worker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(DedicatedKryo.class);
		if (workerAnnotation != null) {
			String prefix = ((DedicatedKryo) workerAnnotation).master()
					.getCanonicalName();
			String hostname = Environment.getPrefixed(prefix,
					Environment.KRYO_SOCKET_HOST);
			String port = Environment.getPrefixed(prefix,
					Environment.KRYO_SOCKET_PORT);
			return hostname + " " + port;
		}
		return null;
	}

	public static String getLocalWorkerParameters(
			Class<? extends Worker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(DedicatedLocal.class);
		if (workerAnnotation != null) {
			return "EMPTY";
		}
		return null;
	}

	public static String getRestWorkerParameters(
			Class<? extends Worker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(DedicatedRest.class);
		if (workerAnnotation != null) {
			return Environment.get(Environment.HTTP_HOST) + " "
					+ Environment.get(Environment.HTTP_PORT);
		}
		return null;
	}

	public static String getSocketWorkerParameters(
			Class<? extends Worker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(DedicatedSocket.class);
		if (workerAnnotation != null) {
			String prefix = ((DedicatedSocket) workerAnnotation)
					.master().getCanonicalName();
			String hostname = Environment.getPrefixed(prefix,
					Environment.SOCKET_HOST);
			String port = Environment.getPrefixed(prefix,
					Environment.SOCKET_PORT);
			return hostname + " " + port;
		}
		return null;
	}

	public static String getWebSocketWorkerParameters(
			Class<? extends Worker<?, ?>> workerClass) throws Exception {
		Annotation workerAnnotation = workerClass
				.getAnnotation(DedicatedWebSocket.class);
		if (workerAnnotation != null) {
			return Environment.get(Environment.HTTP_HOST) + " "
					+ Environment.get(Environment.HTTP_PORT);
		}
		return null;
	}
}
